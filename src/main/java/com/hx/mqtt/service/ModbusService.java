package com.hx.mqtt.service;

/**
 * Modbus通信服务接口
 */
public interface ModbusService {

    /**
     * 读取保持寄存器
     * @param ip 设备IP地址
     * @param port 端口号
     * @param unitId 单元ID
     * @param address 寄存器地址
     * @param quantity 读取数量
     * @return 寄存器值数组
     */
    int[] readHoldingRegisters(String ip, int port, int unitId, int address, int quantity);

    /**
     * 写入单个保持寄存器
     * @param ip 设备IP地址
     * @param port 端口号
     * @param unitId 单元ID
     * @param address 寄存器地址
     * @param value 写入值
     * @return 是否成功
     */
    boolean writeSingleRegister(String ip, int port, int unitId, int address, int value);

    /**
     * 读取单个保持寄存器
     * @param ip 设备IP地址
     * @param port 端口号
     * @param unitId 单元ID
     * @param address 寄存器地址
     * @return 寄存器值
     */
    Integer readSingleRegister(String ip, int port, int unitId, int address);
}
