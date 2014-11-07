package com.ftdi;


import java.io.IOException;

import android.util.Log;


/**
 * @author      FTDI Ltd. <support1@ftdichip.com>
 * @version     1.0                   
 * @since       2011-09-20
 */
public class D2xx {

	
	/* **********************************************************************
	 *
	 * Load the JNI D2XX library
	 * 
	 * **********************************************************************
	 */
	/**
	 * Load the JNI D2XX library
	 */
	static {
		// load the JNI library
		Log.i("ftd2xx-java", "loading JNI library...");
		System.load("/data/data/br.com.ilhasoft.saudeplus/lib/libftd2xx-jni.so");
//		System.load("/sdcard/Android/data/com.ftdi.d2xx/libftd2xx-jni.so");
		Log.i("ftd2xx-java", "JNI library loaded!");
	}
	
	
	/* **********************************************************************
	 *
	 * Internal properties
	 * 
	 * **********************************************************************
	 */
	
	/** Native device handle - initialised to an invalid value */
	protected int ftHandle = 0;
	
	/** Native event handle - initialised to an invalid value */
	protected int ftEventHandle = 0;
	
	
	/* **********************************************************************
	 * 
	 * D2XX Data Types
	 * 
	 * **********************************************************************
	 */
	
	/** D2XX Exception class */
	public static class D2xxException extends IOException {

		private static final long serialVersionUID = 1L;
		public D2xxException(){}
		public D2xxException(String ftStatusMsg) {
			super(ftStatusMsg);
		}
	}
	
	
	/** Java implementation of the FT_DEVICE_LIST_INFO_NODE structure */	
	public static class FtDeviceInfoListNode {
		public int flags;
		public int type;
		public int id;
		public int location;
		public String serialNumber;
		public String description;
		public int handle;		
	}
	
	
	/** Java implementation of the FT_PROGRAM_DATA structure */
	public static class FtProgramData {
	// Common fields
		public short vendorId;
		public short productId;
		public String manufacturer;
		public String manufacturerId;
		public String description;
		public String serialNumber;
		public short maxPower;
		public boolean selfPowered;
		public boolean remoteWakeup;
	// Rev4 (FT232B) extensions
		public boolean rev4;
		public boolean pullDownEnable;
		public boolean serNumEnable;
		public boolean USBVersionEnable;
		public short USBVersion;
	// Rev 5 (FT2232) extensions
		public boolean rev5;
		public boolean pullDownEnable5;
		public boolean serNumEnable5;
		public boolean USBVersionEnable5;
		public short USBVersion5;
		public boolean AIsHighCurrent;
		public boolean BIsHighCurrent;
		public boolean ifAIsFifo;
		public boolean ifAIsFifoTar;
		public boolean ifAIsFastSer;
		public boolean AIsVCP;
		public boolean ifBIsFifo;
		public boolean ifBIsFifoTar;
		public boolean ifBIsFastSer;
		public boolean BIsVCP;
	// Rev 6 (FT232R) extensions
		public boolean useExtOsc;
		public boolean highDriveIOs;
		public boolean pullDownEnableR;
		public boolean serNumEnableR;
		public boolean invertTXD;
		public boolean invertRXD;
		public boolean invertRTS;
		public boolean invertCTS;
		public boolean invertDTR;
		public boolean invertDSR;
		public boolean invertDCD;
		public boolean invertRI;
		public byte cbus0;
		public byte cbus1;
		public byte cbus2;
		public byte cbus3;
		public byte cbus4;	
		public boolean RIsD2XX;
	// Rev 7 (FT2232H) Extensions
		public boolean pullDownEnable7;
		public boolean serNumEnable7;
		public boolean ALSlowSlew;
		public boolean ALSchmittInput;
		public byte ALDriveCurrent;
		public boolean AHSlowSlew;
		public boolean AHSchmittInput;
		public byte AHDriveCurrent;
		public boolean BLSlowSlew;
		public boolean BLSchmittInput;
		public byte BLDriveCurrent;
		public boolean BHSlowSlew;
		public boolean BHSchmittInput;
		public byte BHDriveCurrent;
		public boolean ifAIsFifo7;
		public boolean ifAIsFifoTar7;
		public boolean ifAIsFastSer7;
		public boolean AIsVCP7;
		public boolean ifBIsFifo7;
		public boolean ifBIsFifoTar7;
		public boolean ifBIsFastSer7;
		public boolean BIsVCP7;
		public boolean powerSaveEnable;
	// Rev 8 (FT4232H) Extensions
		public boolean pullDownEnable8;
		public boolean serNumEnable8;
		public boolean ASlowSlew;
		public boolean ASchmittInput;
		public byte ADriveCurrent;
		public boolean BSlowSlew;
		public boolean BSchmittInput;
		public byte BDriveCurrent;
		public boolean CSlowSlew;
		public boolean CSchmittInput;
		public byte CDriveCurrent;
		public boolean DSlowSlew;
		public boolean DSchmittInput;
		public byte DDriveCurrent;
		public boolean ARIIsTXDEN;
		public boolean BRIIsTXDEN;
		public boolean CRIIsTXDEN;
		public boolean DRIIsTXDEN;
		public boolean AIsVCP8;
		public boolean BIsVCP8;
		public boolean CIsVCP8;
		public boolean DIsVCP8;
	// Rev 9 (FT232H) Extensions
		public boolean pullDownEnableH;
		public boolean serNumEnableH;
		public boolean ACSlowSlewH;
		public boolean ACSchmittInputH;
		public byte ACDriveCurrentH;
		public boolean ADSlowSlewH;
		public boolean ADSchmittInputH;
		public byte ADDriveCurrentH;
		public byte cbus0H;
		public byte cbus1H;
		public byte cbus2H;
		public byte cbus3H;
		public byte cbus4H;
		public byte cbus5H;
		public byte cbus6H;
		public byte cbus7H;
		public byte cbus8H;
		public byte cbus9H;
		public boolean isFifoH;
		public boolean isFifoTarH;
		public boolean isFastSerH;
		public boolean isFT1248H;
		public boolean FT1248CpolH;
		public boolean FT1248LsbH;
		public boolean FT1248FlowControlH;
		public boolean isVCPH;
		public boolean powerSaveEnableH;
	}
	
	
	
	/* **********************************************************************
	 * 
	 * D2XX Constants
	 * 
	 * **********************************************************************
	 */
	
	/**  FT_OpenEx flags */
	public static final int 
		FT_OPEN_BY_SERIAL_NUMBER		= 1,
		FT_OPEN_BY_DESCRIPTION			= 2,
		FT_OPEN_BY_LOCATION				= 4;
	
	
	/** FT_ListDevices flags */
	public static final int 
		FT_LIST_NUMBER_ONLY				= 0x80000000,
		FT_LIST_BY_INDEX				= 0x40000000,
		FT_LIST_ALL						= 0x20000000;
	
	
	/** Data bits */
	public static final byte 
		FT_DATA_BITS_7					= 7,
		FT_DATA_BITS_8					= 8;
	
	
	/** Stop bits */
	public static final byte 
		FT_STOP_BITS_1					= 0,
		FT_STOP_BITS_2					= 2;
	
	
	/** Parity */
	public static final byte
		FT_PARITY_NONE					= 0,
		FT_PARITY_ODD					= 1,
		FT_PARITY_EVEN					= 2,
		FT_PARITY_MARK					= 3,
		FT_PARITY_SPACE					= 4;

	
	/** Flow Control */
	public static final short 
		FT_FLOW_NONE					= 0x0000,
		FT_FLOW_RTS_CTS					= 0x0100,
		FT_FLOW_DTR_DSR					= 0x0200,
		FT_FLOW_XON_XOFF				= 0x0400;

	
	/** Purge flags */
	public static final byte 
		FT_PURGE_RX						= 1,
		FT_PURGE_TX						= 2;

	
	/** Modem status bits */
	public static final byte 
		FT_CTS							= 0x10,
		FT_DSR							= 0x20,
		FT_RI							= 0x40,
		FT_DCD							= (byte) 0x80;


	/** Line status bits */
	public static final byte 
		FT_OE 							= 0x02,
		FT_PE 							= 0x04,
		FT_FE 							= 0x08,
		FT_BI 							= 0x10;
	
	
	/** Events */
	public static final byte
		FT_EVENT_RXCHAR					= 1,
		FT_EVENT_MODEM_STATUS			= 2,
		FT_EVENT_LINE_STATUS			= 4;
	
	
	/** Device info list flags */
	public static final byte
		FT_FLAGS_OPENED					= 1,
		FT_FLAGS_HI_SPEED				= 2;

	
	/** Device types */
	public static final int
		FT_DEVICE_232B					= 0,
		FT_DEVICE_8U232AM				= 1,
	    FT_DEVICE_8U100AX				= 2,
	    FT_DEVICE_UNKNOWN				= 3,	
		FT_DEVICE_2232					= 4,
		FT_DEVICE_232R					= 5,
		FT_DEVICE_2232H					= 6,
		FT_DEVICE_4232H					= 7,
		FT_DEVICE_232H					= 8;
	
	
	/** Bit Modes */
	public static final byte
		FT_BITMODE_RESET				= 0x00,
		FT_BITMODE_ASYNC_BITBANG		= 0x01,
		FT_BITMODE_MPSSE				= 0x02,
		FT_BITMODE_SYNC_BITBANG			= 0x04,
		FT_BITMODE_MCU_HOST				= 0x08,
		FT_BITMODE_FAST_SERIAL			= 0x10,
		FT_BITMODE_CBUS_BITBANG			= 0x20,
		FT_BITMODE_SYNC_FIFO			= 0x40;

	
	/** FT232R CBUS Options EEPROM values */
	public static final byte
		FT_232R_CBUS_TXDEN				= 0x00,	//	Tx Data Enable
		FT_232R_CBUS_PWRON				= 0x01,	//	Power On
		FT_232R_CBUS_RXLED				= 0x02,	//	Rx LED
		FT_232R_CBUS_TXLED				= 0x03,	//	Tx LED
		FT_232R_CBUS_TXRXLED			= 0x04,	//	Tx and Rx LED
		FT_232R_CBUS_SLEEP				= 0x05,	//	Sleep
		FT_232R_CBUS_CLK48				= 0x06,	//	48MHz clock
		FT_232R_CBUS_CLK24				= 0x07,	//	24MHz clock
		FT_232R_CBUS_CLK12				= 0x08,	//	12MHz clock
		FT_232R_CBUS_CLK6				= 0x09,	//	6MHz clock
		FT_232R_CBUS_IOMODE				= 0x0A,	//	IO Mode for CBUS bit-bang
		FT_232R_CBUS_BITBANG_WR			= 0x0B,	//	Bit-bang write strobe
		FT_232R_CBUS_BITBANG_RD			= 0x0C;	//	Bit-bang read strobe

	
	/** FT232H CBUS Options EEPROM values */
	public static final byte
		FT_232H_CBUS_TRISTATE			= 0x00,	//	Tristate
		FT_232H_CBUS_TXLED				= 0x01,	//	Tx LED
		FT_232H_CBUS_RXLED				= 0x02,	//	Rx LED
		FT_232H_CBUS_TXRXLED			= 0x03,	//	Tx and Rx LED
		FT_232H_CBUS_PWREN				= 0x04,	//	Power Enable
		FT_232H_CBUS_SLEEP				= 0x05,	//	Sleep
		FT_232H_CBUS_DRIVE_0			= 0x06,	//	Drive pin to logic 0
		FT_232H_CBUS_DRIVE_1			= 0x07,	//	Drive pin to logic 1
		FT_232H_CBUS_IOMODE				= 0x08,	//	IO Mode for CBUS bit-bang
		FT_232H_CBUS_TXDEN				= 0x09,	//	Tx Data Enable
		FT_232H_CBUS_CLK30				= 0x0A,	//	30MHz clock
		FT_232H_CBUS_CLK15				= 0x0B,	//	15MHz clock
		FT_232H_CBUS_CLK7_5				= 0x0C;	//	7.5MHz clock

	
	/** Drive current values for FT2232H, FT4232H and FT232H EEPROMs */
	public static final byte
		FT_DRIVE_CURRENT_4MA			= 0x04,
		FT_DRIVE_CURRENT_8MA			= 0x08,
		FT_DRIVE_CURRENT_12MA			= 0x0C,
		FT_DRIVE_CURRENT_16MA			= 0x10;
	
	
	
	
	/* **********************************************************************
	 * 
	 * Static D2XX Methods
	 * 
	 * **********************************************************************/
		
	/**
	 * This method allows a custom VID and PID combination within the internal
	 * device list table. This will allow the driver to load for the specified 
	 * VID and PID combination.
	 * 
	 * @param	vendorId		The vendor ID that the driver should match with
	 * @param	productId		The product ID that the driver should match with
	 * @throws	D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native static void setVIDPID(int vendorId, int productId) throws D2xxException;
	
	
	/**
	 * This method retrieves the current VID and PID combination from within the
	 * internal device list table. The VID and PID to match can be set using {@link #setVIDPID}
	 * 
	 * @return					2-element array containing the VID in the first element
	 * 							and the PID in the second element.
	 * @throws	D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native static int[] getVIDPID() throws D2xxException;
	
	
	/** 
	 * This method builds an internal device information list and returns the number 
	 * of D2XX devices connected to the system. The list contains information 
	 * about both unopened and opened devices. Device information may be retrieved via the
	 * {@link #getDeviceInfoList} or {@link #getDeviceInfoDetail} methods.
	 * 
	 * @return					The number of devices represented in the device information
	 * 							list.  This should be used to ensure sufficient storage for the
	 * 							device list returned by {@link #getDeviceInfoList}.
	 * @throws	D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native static int createDeviceInfoList() throws D2xxException;
	
	
	/**
	 * This method returns the device list created with a prior call to {@link #createDeviceInfoList}.
	 * The list contains all available information for all of the available devices at the
	 * time that {@link #createDeviceInfoList} was called.
	 * Note that the flags element of each {@link #FtDeviceInfoListNode} object in the list 
	 * is a bit-mask of {@link #FT_FLAGS_OPENED} and {@link #FT_FLAGS_HI_SPEED}.
	 * 
	 * @param	numDevs			The number of devices represented in the device information
	 * 							list.  
	 * @param	deviceList		An array of {@link #FtDeviceInfoListNode}. Following a successful 
	 *							call, this will contain information on all available devices.
	 * @return					The number of devices represented in the device information
	 * 							list as returned from the native FT_GetDeviceInfoList call. 
	 * @throws	D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native static int getDeviceInfoList(int numDevs, FtDeviceInfoListNode[] deviceList) throws D2xxException;
	
	
	/**
	 * This method returns information for a single device from the internal device list which
	 * was created by a previous call to {@link #createDeviceInfoList}.
	 * Note that the flags element of the {@link #FtDeviceInfoListNode} object is a bit-mask of
	 * {@link #FT_FLAGS_OPENED} and {@link #FT_FLAGS_HI_SPEED}.
	 * 
	 * @param	index			The index of the device in the list that information should 
	 * 							be returned for.
	 * @return					A {@link #FtDeviceInfoListNode} object containing the information
	 * 							available for the device at the specified index in the list.
	 * @throws	D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native static FtDeviceInfoListNode getDeviceInfoListDetail(int index) throws D2xxException;
	
	
	/**
	 * This method returns the version number for the native D2XX library in use.
	 * 
	 * @return					A 32-bit number representing the library version in binary
	 * 							coded decimal format.
	 * @throws	D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native static int getLibraryVersion() throws D2xxException;
	
	
	
	
	/* **********************************************************************
	 * 
	 * D2XX Methods
	 * 
	 * **********************************************************************/
	
	/**
	 * This method opens the device at the specified index for use and obtains a native
	 * handle to it.
	 * 
	 * @param	index			The index of the device to be opened. The index is 0 based.
	 * @throws	D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native void openByIndex(int index) throws D2xxException;
	
	
	/**
	 * This method opens the device with the specified serial number for use and obtains 
	 * a native handle to it.
	 * 
	 * @param	serialNumber	The serial number of the device to be opened.	
	 * @throws	D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native void openBySerialNumber(String serialNumber) throws D2xxException;
	
	
	/**
	 * This method opens the device with the specified description for use and obtains 
	 * a native handle to it.
	 * 
	 * @param	description		The description of the device to be opened.	
	 * @throws	D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native void openByDescription(String description) throws D2xxException;
	
	
	/**
	 * This method opens the device at the specified location for use and obtains 
	 * a native handle to it.
	 * 
	 * @param	location		The location of the device to be opened.
	 * @throws	D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native void openByLocation(int location) throws D2xxException;
	
	
	/**
	 * This method closes a native device handle obtained with a previous call to 
	 * {@link #openByIndex}, {@link #openBySerialNumber}, {@link #openByDescription} or
	 * {@link #openByLocation}.
	 * 
	 * @throws	D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native void close() throws D2xxException;
	
	
	/**
	 * This method reads data from the device in to the Java application buffer.
	 * The device must be open to read data from it.
	 * 
	 * @param	data			A data buffer containing the bytes read from the device.
	 * @param	bytesToRead		The number of bytes that the application is requesting 
	 * 							to be read from the device.
	 * @return					The number of bytes successfully read from the device.
	 * @throws	D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native int read(byte[] data, int bytesToRead) throws D2xxException;
	
	
	/**
	 * This method writes data to the device from the Java application buffer.
	 * The device must be open to write data to it.
	 * 
	 * @param	data			A data buffer containing the bytes to write to the device.
	 * @param	bytesToWrite	The number of bytes that the application is requesting 
	 * 							to write to the device.
	 * @return					The number of bytes successfully written to the device.
	 * @throws	D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native int write(byte[] data, int bytesToWrite) throws D2xxException;
	
	
	/**
	 * This method sends a vendor command to the device to change the baud rate generator
	 * value. Note that the baud rate is only meaningful when the device is in UART or
	 * bit-bang mode.
	 * 
	 * @param	baudRate		The baud rate value to set for the device.  This must be a
	 * 							value >184 baud. The maximum baud rate for full speed devices
	 * 							is 3Mbaud, for hi-speed devices it is 12Mbaud.
	 * @throws	D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native void setBaudRate(int baudRate) throws D2xxException;
	
	
	/**
	 * This method dictates the data format that the device will use.  Communication errors will
	 * occur if these parameters do not match those used by the external system
	 * Note that these data characteristics are only meaningful when the device is in UART mode.
	 * 
	 * @param	dataBits		Valid data bit values are {@link #FT_DATA_BITS_7} or
	 * 							{@link #FT_DATA_BITS_8}.
	 * @param	stopBits		Valid stop bit values are {@link #FT_STOP_BITS_1} or
	 * 							{@link #FT_STOP_BITS_2}.
	 * @param	parity			Valid parity values are {@link #FT_PARITY_NONE}, {@link #FT_PARITY_ODD},
	 * 							{@link #FT_PARITY_EVEN}, {@link #FT_PARITY_MARK} or
	 * 							{@link #FT_PARITY_SPACE}.
	 * @throws	D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native void setDataCharacteristics(byte dataBits, byte stopBits, byte parity) throws D2xxException;
	
	
	/**
	 * This method specifies the flow control method that the device should use to prevent data loss.
	 *  
	 * @param	flowControl		Valid flow control values are {@link #FT_FLOW_NONE}, {@link #FT_FLOW_RTS_CTS},
	 * 							{@link #FT_FLOW_DTR_DSR} or {@link #FT_FLOW_XON_XOFF}.
	 * @param	xon				Specifies the character to use for XOn if {@link #FT_FLOW_XON_XOFF}
	 * 							is enabled.
	 * @param	xoff			Specifies the character to use for XOff if {@link #FT_FLOW_XON_XOFF}
	 * 							is enabled.
	 * @throws	D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native void setFlowControl(short flowControl, byte xon, byte xoff) throws D2xxException;
	
	
	/**
	 * This method specifies the timeout values to be used for read and write operations.
	 * Default timeout values are 0 which is interpreted as infinite; in this case read and write
	 * calls will block until all of the requested data has been transferred.
	 * 
	 * @param	readTimeout		The value in ms to apply to read operations.
	 * @param	writeTimeout	The value in ms to apply to write operations.
	 * @throws	D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native void setTimeouts(int readTimeout, int writeTimeout) throws D2xxException;
	
	
	/**
	 * This method allows the DTR modem control line to be manually asserted.
	 * Note that this method is only meaningful when the device is in UART mode.
	 * 
	 * @throws	D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native void setDtr() throws D2xxException;
	
	
	/**
	 * This method allows the DTR modem control line to be manually de-asserted.
	 * Note that this method is only meaningful when the device is in UART mode.
	 * 
	 * @throws	D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native void clrDtr() throws D2xxException;
	
	
	/**
	 * This method allows the RTS modem control line to be manually asserted.
	 * Note that this method is only meaningful when the device is in UART mode.
	 * 
	 * @throws	D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native void setRts() throws D2xxException;

	
	/**
	 * This method allows the RTS modem control line to be manually de-asserted.
	 * Note that this method is only meaningful when the device is in UART mode.
	 * 
	 * @throws	D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native void clrRts() throws D2xxException;
	
	
	/**
	 * This method generates a BREAK condition on the device UART.
	 * Note that this method is only meaningful when the device is in UART mode.
	 * 
	 * @throws D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native void setBreakOn() throws D2xxException;
	
	
	/**
	 * This method resets the BREAK condition on the device UART.
	 * Note that this method is only meaningful when the device is in UART mode.
	 * 
	 * @throws D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native void setBreakOff() throws D2xxException;
	
	
	/**
	 * This method retrieves the current modem and line status values for the device.
	 * Note that this method is only meaningful when the device is in UART mode.
	 * 
	 * @return					2-element array containing the modem status in the first element
	 * 							and the line status in the second element.
	 * 							The modem status is a bit-mask of {@link #FT_CTS}, {@link #FT_DSR},
	 * 							{@link #FT_RI} and {@link #FT_DCD}.
	 * 							The line status is a bit-mask of {@link #FT_OE}, {@link #FT_PE},
	 * 							{@link #FT_FE} and {@link #FT_BI}.
	 * @throws	D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native byte[] getModemStatus() throws D2xxException;
	
	
	/**
	 * This method retrieves the number of bytes available to read from the native driver 
	 * Rx buffer.
	 * 
	 * @return					The number of bytes available in the driver Rx buffer.
	 * 							A call to {@link #read} requesting up to this number of bytes
	 *							will return with the data immediately.
	 * @throws	D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native int getQueueStatus() throws D2xxException;
	
	
	/**
	 * This method retrieves the number of bytes available to read from the native driver 
	 * Rx buffer, the number of bytes waiting in the native driver Tx buffer and the 
	 * type of the last event that triggered.
	 * 
	 * @return					3-element array containing the Rx queue status in the first element,
	 * 							the Tx queue status in the second element and the event status in the third.
	 * 							The event status is a bit-mask of {@link #FT_EVENT_RXCHAR},
	 * 							{@link #FT_EVENT_MODEM_STATUS} and {@link #FT_EVENT_LINE_STATUS}. 
	 * @throws	D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native int[] getStatus() throws D2xxException;
	
	
	/**
	 * Discards any data form the specified driver buffer and also flushes data 
	 * from the device.
	 * 
	 * @param	flags			Specifies the queue to purge. flags is a bit-mask of
	 * 							{@link #FT_PURGE_RX} and {@link #FT_PURGE_TX}.
	 * @throws	D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native void purge(byte flags) throws D2xxException;
	
	
	/**
	 * This method sends vendor commands to the device to cause a reset and flush any data
	 * from the device buffers.
	 * 
	 * @throws	D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native void resetDevice() throws D2xxException;
	
	
	/**
	 * This method stops the native driver's IN thread and prevents USB IN requests 
	 * being issued to the device.  No data will be received from the device if the IN 
	 * thread is stopped.
	 * 
	 * @throws	D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native void stopInTask() throws D2xxException;
	
	
	/**
	 * This method restarts the native driver's IN thread following a successful call
	 * to {@link #stopInTask} 
	 * 
	 * @throws	D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native void restartInTask() throws D2xxException;
	
	
	/**
	 * This method retrieves information on the device that is currently open.
	 * 
	 * @return					A {@link #FtDeviceInfoListNode} object containing the information
	 * 							available for the device. Note that the flags and location
	 * 							fields are not used by this method.
	 * @throws	D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native FtDeviceInfoListNode getDeviceInfo() throws D2xxException;
	
	
	/**
	 * This method specifies the event character and error replacement characters for the device 
	 * to use. 
	 * When the device detects an event character being received, this will trigger an IN 
	 * to the USB Host regardless of the number of bytes in the device's buffer or the latency 
	 * timer value.
	 * When the device detects an error ({@link #FT_OE}, {@link #FT_PE}, {@link #FT_FE} or 
	 * {@link #FT_BI}), the error character will be inserted in to the data stream to the
	 * USB host.
	 *  
	 * @param	eventChar		The character for which the device to trigger an IN.
	 * @param	eventCharEnable	Enable or disable the use of the event character.
	 * @param	errorChar		The character that will be inserted in the data stream
	 * 							on the detection of an error.
	 * @param	errorCharEnable	Enable or disable the use of the error replacement character.
	 * @throws	D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native void setChars(byte eventChar, byte eventCharEnable, byte errorChar, byte errorCharEnable) throws D2xxException;
	
	
	/**
	 * This method allows the size of the USB requests used by the native driver to be modified.
	 * The default values for both IN and OUT transfer sizes are 4096 bytes.
	 * 
	 * @param	inTransferSize	The USB transfer size to be used for IN requests.  
	 * 							This must be a multiple the maxPacketSize for the device
	 * 							(64 bytes for a full-speed device, 512 bytes for a hi-speed device).
	 * @param	outTransferSize	The USB transfer size to be used for OUT requests.  
	 * 							This must be a multiple the maxPacketSize for the device
	 * 							(64 bytes for a full-speed device, 512 bytes for a hi-speed device).
	 * @throws	D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native void setUSBParameters(int inTransferSize, int outTransferSize) throws D2xxException;
	
	
	/**
	 * This method returns the version number for the native D2XX driver in use.
	 * 
	 * @return					A 32-bit number representing the driver version in binary
	 * 							coded decimal format.
	 * @throws	D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native int getDriverVersion() throws D2xxException;


	/**
	 * This method specifies events for the native driver to signal that they have occurred.
	 * Once the event mask has been set, the event can be waited on using the {@link #waitEvent}
	 * or {@link #waitEventTimed} methods.
	 * 
	 * @param	mask			Specifies the events to wait on. This is a bit-mask of
	 * 							{@link #FT_EVENT_RXCHAR}, {@link #FT_EVENT_MODEM_STATUS} 
	 * 							and {@link #FT_EVENT_LINE_STATUS}.
	 * @throws	D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native void setEventNotification(int mask) throws D2xxException;
	
	
	// native support functions for use with setEventNotification that do not call
	// the native D2XX driver
	/**
	 * This method blocks until an event specified in {@link #setEventNotification} occurs.
	 * Once this method returns, the event type that has occurred can be determined with a
	 * call to {@link #getStatus}.
	 * 
	 */
	public native void waitEvent();
	
	
	/**
	 * This method blocks until an event specified in {@link #setEventNotification} occurs
	 * or the specified timeout has elapsed. Once this method returns, the event type that 
	 * has occurred can be determined with a call to {@link #getStatus}.
	 * 
	 * @param	timeout			Specify the maximum time in ms to wait on the event type set in 
	 * 							{@link #setEventNotification} occurring.
	 * @return					The return value is TRUE if the event fired. FALSE is returned 
	 * 							if a timeout occurred before the event fired.
	 */
	public native boolean waitEventTimed(int timeout);
	
	
	
	
	/* **********************************************************************
	 * 
	 * Extended methods for FT232B devices and later
	 * 
	 * **********************************************************************/
	
	/**
	 * This method allows the latency timer value for the device to be specified.
	 * The latency timer is the mechanism that returns short packets to the USB host.
	 * The default value is 16ms.
	 * 
	 * @param 	latency			The new value to use for the latency timer. The valid 
	 * 							range for this is 2ms - 255ms.
	 * @throws	D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native void setLatencyTimer(byte latency) throws D2xxException;
	
	
	/**
	 * This method retrieves the current latency timer value from the device.
	 * The latency timer is the mechanism that returns short packets to the USB host.
	 * The default value is 16ms.
	 * 
	 * @return					The current latency timer value.
	 * @throws	D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native byte getLatencyTimer() throws D2xxException;
	
	
	/**
	 * This method allows the device to use alternative interface modes such as bit-bang,
	 * MPSSE and CPU target mode.
	 * Note that not all modes are available on all devices; please consult the device
	 * data sheet for more information. 
	 * 
	 * @param	mask			Bit-mask that specifies which pins are input (0) and which 
	 * 							are output (1). Required for bit-bang modes. In the case of
	 * 							CBUS bit-bang, the upper nibble of this value controls which
	 * 							pins are inputs and outputs, while the lower nibble controls
	 * 							which of the outputs are high and low.
	 * @param	bitMode			The desired device mode. This can be one of the following: 
	 * 							{@link #FT_BITMODE_RESET}, {@link #FT_BITMODE_ASYNC_BITBANG},
	 * 							{@link #FT_BITMODE_MPSSE}, {@link #FT_BITMODE_SYNC_BITBANG},
	 * 							{@link #FT_BITMODE_MCU_HOST}, {@link #FT_BITMODE_FAST_SERIAL},
	 * 							{@link #FT_BITMODE_CBUS_BITBANG} or {@link #FT_BITMODE_SYNC_FIFO}.
	 * @throws	D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native void setBitMode(byte mask, byte bitMode) throws D2xxException;
	
	
	/**
	 * This method retrieves the instantaneous value of the data bus pins. This method 
	 * ultimately calls the native D2XX function FT_GetBitMode.
	 * 
	 * @return					The value read from the device pins.
	 * @throws	D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native byte getPinStates() throws D2xxException;
	
	
	
	
	/* **********************************************************************
	 * 
	 * EEPROM methods
	 * 
	 * **********************************************************************/
	
	/**
	 * This method reads a WORD from the device EEPROM at the specified address.
	 * 
	 * @param	address			The EEPROM address to read from.
	 * @return					The EEPROM data WORD read from the specified address.
	 * @throws	D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native short eepromReadWord(int address) throws D2xxException;
	
	
	/**
	 * This method writes a WORD to the device EEPROM at the specified address.
	 * 
	 * @param	address			The EEPROM address to write the new data to.
	 * @param	data			The data WORD to write to the EEPROM at the address specified.
	 * @throws	D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native void eepromWriteWord(int address, short data) throws D2xxException;
	
	
	/**
	 * This method erases the device EEPROM. After erasing, all values read will be 0xFFFF.
	 * Note that the FT232R and FT245R devices cannot have their EEPROMs erased since the
	 * EEPROM is internal to the device.
	 * 
	 * @throws	D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native void eepromErase() throws D2xxException;
	
	
	/**
	 * This method reads the entire device EEPROM and decodes its settings in to fields in 
	 * a {@link #FtProgramData} object.
	 * 
	 * @return					A {@link #FtProgramData} object containing the parsed EEPROM
	 * 							settings for the device.
	 * @throws	D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native FtProgramData eepromRead() throws D2xxException;
	
	
	/**
	 * This method encodes the settings from a {@link #FtProgramData} object and writes
	 * them to the device EEPROM.
	 * 
	 * @param	eeData			A {@link #FtProgramData} object containing the EEPROM
	 * 							settings to be written to the device.
	 * @throws	D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native void eepromWrite(FtProgramData eeData) throws D2xxException;
	
	
	/**
	 * This method retrieves the amount of additional space available in the device EEPROM.
	 * This space (the user area) can be used to store application specific data.
	 * 
	 * @return					The number of unused EEPROM bytes available to the user.
	 * @throws	D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native int eepromGetUserAreaSize() throws D2xxException;
	
	
	/**
	 * This method retrieves the contents of the device EEPROM user area. The number of 
	 * bytes returned will match the user area size returned from {@link #eepromGetUserAreaSize}
	 * 
	 * @return					An array of bytes containing the user area data from the
	 * 							device EEPROM.
	 * @throws	D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native byte[] eepromReadUserArea() throws D2xxException;
	
	
	/**
	 * This method writes data to the device EEPROM user area. Once written, the data 
	 * can be retrieved with a call to {@link #eepromReadUserArea}.
	 * 
	 * @param	uadata			The data to be written to the device EEPROM user area. If
	 * 							the amount of data being written is larger than the available
	 * 							space in the device EEPROM user area, the data will be 
	 * 							truncated to the user area size.
	 * @throws	D2xxException	If the native D2XX call completed with a status other 
	 * 							than FT_OK.
	 */
	public native void eepromWriteUserArea(byte[] uadata) throws D2xxException;	
		
}
