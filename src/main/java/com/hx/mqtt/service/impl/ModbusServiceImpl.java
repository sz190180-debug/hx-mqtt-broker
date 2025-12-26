package com.hx.mqtt.service.impl;

import com.hx.mqtt.config.MachineControlConfig;
import com.hx.mqtt.service.ModbusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Modbus TCP通信服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModbusServiceImpl implements ModbusService {

    private final MachineControlConfig machineControlConfig;

    // 连接池，复用TCP连接
    private final ConcurrentHashMap<String, Socket> connectionPool = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    // 连接超时时间（毫秒）与读取超时时间（毫秒）从配置读取

    @Override
    public int[] readHoldingRegisters(String ip, int port, int unitId, int address, int quantity) {
        String key = getConnectionKey(ip, port);
        ReentrantLock lock = lockMap.computeIfAbsent(key, k -> new ReentrantLock());

        lock.lock();
        try {
            Socket socket = getConnection(ip, port);
            if (socket == null) {
                log.error("无法连接到Modbus设备: {}:{}", ip, port);
                return null;
            }
            socket.setSoTimeout(machineControlConfig.getModbus().getReadTimeout());

            // 构建Modbus TCP请求
            byte[] request = buildReadHoldingRegistersRequest(unitId, address, quantity);

            // 发送请求
            socket.getOutputStream().write(request);
            socket.getOutputStream().flush();

            // 读取响应
            byte[] response = new byte[1024];
            int bytesRead = socket.getInputStream().read(response);

            if (bytesRead < 9) {
                log.error("Modbus响应数据长度不足: {}", bytesRead);
                return null;
            }

            // 解析响应
            return parseReadHoldingRegistersResponse(response, quantity);

        } catch (Exception e) {
            log.error("读取Modbus寄存器失败: {}:{}, 地址: {}", ip, port, address, e);
            // 连接异常时移除连接
            removeConnection(key);
            return null;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean writeSingleRegister(String ip, int port, int unitId, int address, int value) {
        String key = getConnectionKey(ip, port);
        ReentrantLock lock = lockMap.computeIfAbsent(key, k -> new ReentrantLock());

        lock.lock();
        try {
            Socket socket = getConnection(ip, port);
            if (socket == null) {
                log.error("无法连接到Modbus设备: {}:{}", ip, port);
                return false;
            }
            socket.setSoTimeout(machineControlConfig.getModbus().getReadTimeout());

            // 构建Modbus TCP写单个寄存器请求
            byte[] request = buildWriteSingleRegisterRequest(unitId, address, value);

            // 发送请求
            socket.getOutputStream().write(request);
            socket.getOutputStream().flush();

            // 读取响应
            byte[] response = new byte[1024];
            int bytesRead = socket.getInputStream().read(response);

            if (bytesRead < 12) {
                log.error("Modbus写寄存器响应数据长度不足: {}", bytesRead);
                return false;
            }

            // 检查响应是否成功
            return isWriteResponseSuccess(response, address, value);

        } catch (Exception e) {
            log.error("写入Modbus寄存器失败: {}:{}, 地址: {}, 值: {}", ip, port, address, value, e);
            // 连接异常时移除连接
            removeConnection(key);
            return false;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Integer readSingleRegister(String ip, int port, int unitId, int address) {
        int[] result = readHoldingRegisters(ip, port, unitId, address, 1);
        return result != null && result.length > 0 ? result[0] : null;
    }

    /**
     * 获取连接
     */
    private Socket getConnection(String ip, int port) {
        String key = getConnectionKey(ip, port);
        Socket socket = connectionPool.get(key);

        // 检查连接是否有效
        if (socket != null && (!socket.isConnected() || socket.isClosed())) {
            connectionPool.remove(key);
            socket = null;
        }

        // 创建新连接
        if (socket == null) {
            try {
                socket = new Socket();
                int connectTimeout = machineControlConfig.getModbus().getConnectTimeout();
                socket.connect(new InetSocketAddress(ip, port), connectTimeout > 0 ? connectTimeout : 3000);
                socket.setSoTimeout(machineControlConfig.getModbus().getReadTimeout());
                connectionPool.put(key, socket);
                log.debug("创建Modbus连接: {}:{}", ip, port);
            } catch (IOException e) {
                log.error("创建Modbus连接失败: {}:{}", ip, port, e);
                return null;
            }
        }

        return socket;
    }

    /**
     * 移除连接
     */
    private void removeConnection(String key) {
        Socket socket = connectionPool.remove(key);
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                log.warn("关闭Modbus连接失败", e);
            }
        }
    }

    /**
     * 获取连接键
     */
    private String getConnectionKey(String ip, int port) {
        return ip + ":" + port;
    }

    /**
     * 构建读保持寄存器请求
     */
    private byte[] buildReadHoldingRegistersRequest(int unitId, int address, int quantity) {
        ByteBuffer buffer = ByteBuffer.allocate(12);

        // MBAP Header
        buffer.putShort((short) 1);      // Transaction ID
        buffer.putShort((short) 0);      // Protocol ID
        buffer.putShort((short) 6);      // Length
        buffer.put((byte) unitId);       // Unit ID

        // PDU
        buffer.put((byte) 0x03);         // Function Code (Read Holding Registers)
        buffer.putShort((short) address); // Starting Address
        buffer.putShort((short) quantity); // Quantity

        return buffer.array();
    }

    /**
     * 构建写单个寄存器请求
     */
    private byte[] buildWriteSingleRegisterRequest(int unitId, int address, int value) {
        ByteBuffer buffer = ByteBuffer.allocate(12);

        // MBAP Header
        buffer.putShort((short) 1);      // Transaction ID
        buffer.putShort((short) 0);      // Protocol ID
        buffer.putShort((short) 6);      // Length
        buffer.put((byte) unitId);       // Unit ID

        // PDU
        buffer.put((byte) 0x06);         // Function Code (Write Single Register)
        buffer.putShort((short) address); // Register Address
        buffer.putShort((short) value);   // Register Value

        return buffer.array();
    }

    /**
     * 解析读保持寄存器响应
     */
    private int[] parseReadHoldingRegistersResponse(byte[] response, int quantity) {
        if (response.length < 9 + quantity * 2) {
            return null;
        }

        int[] values = new int[quantity];
        int offset = 9; // MBAP Header (6) + Unit ID (1) + Function Code (1) + Byte Count (1)

        for (int i = 0; i < quantity; i++) {
            values[i] = ((response[offset] & 0xFF) << 8) | (response[offset + 1] & 0xFF);
            offset += 2;
        }

        return values;
    }

    /**
     * 检查写寄存器响应是否成功
     */
    private boolean isWriteResponseSuccess(byte[] response, int address, int value) {
        if (response.length < 12) {
            return false;
        }

        // 检查功能码
        if (response[7] != 0x06) {
            return false;
        }

        // 检查地址和值是否匹配
        int responseAddress = ((response[8] & 0xFF) << 8) | (response[9] & 0xFF);
        int responseValue = ((response[10] & 0xFF) << 8) | (response[11] & 0xFF);

        return responseAddress == address && responseValue == value;
    }
}
