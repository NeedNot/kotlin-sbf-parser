package net.neednot.sbfparser

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortDataListener
import com.fazecast.jSerialComm.SerialPortEvent
import kotlin.system.measureTimeMillis


fun main() {
    listSerialPorts()
    val port = readln()
    readSerialData(port)
}

fun listSerialPorts() {
    val ports = SerialPort.getCommPorts()
    if (ports.isEmpty()) {
        println("No serial ports found.")
    } else {
        println("Available ports:")
        ports.forEach { port -> println(port.systemPortPath) }
    }
}

fun readSerialData(portName: String) {
    val serialPort = SerialPort.getCommPort(portName)
    serialPort.baudRate = 115200
    serialPort.openPort()

    if (!serialPort.isOpen) {
        println("Failed to open port $portName")
        return
    }

    val sbfParser = SBFParser()
    var lastSuccess: Long = System.currentTimeMillis()

    serialPort.addDataListener(object : SerialPortDataListener {
        override fun getListeningEvents(): Int {
            return SerialPort.LISTENING_EVENT_DATA_RECEIVED
        }

        override fun serialEvent(serialPortEvent: SerialPortEvent) {
            if (serialPortEvent.eventType == SerialPort.LISTENING_EVENT_DATA_RECEIVED) {
                val data = serialPortEvent.receivedData
                measureTimeMillis {
                    sbfParser.addData(data)
                }.also { println("parsing $it ms") }

                val blocks = sbfParser.getBlocks()
                if (blocks.isNotEmpty()) {
                    blocks.forEach { block ->
                        println("took ${System.currentTimeMillis() - lastSuccess} ms")
                        lastSuccess = System.currentTimeMillis()
                        val body = block.body
                        when (body) {
                            is BaseVectorGeod -> {
                                val vectorInfoGeod = body.vectorInfoGeod.firstOrNull()
                                println("East ${vectorInfoGeod?.deltaEast}, North ${vectorInfoGeod?.deltaNorth}, Up ${vectorInfoGeod?.deltaUp}")
                            }
                            is QualityInd -> {
                                println("Quality $body")
                            }
                            else -> println("Unknown block")
                        }
                    }
                } else {
                    println("blocks empty")
                }
            }
        }
    })
}