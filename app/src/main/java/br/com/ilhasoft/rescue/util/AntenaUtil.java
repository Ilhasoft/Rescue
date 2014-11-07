package br.com.ilhasoft.rescue.util;

import android.util.Log;

import com.ftdi.D2xx;
import com.ftdi.D2xx.D2xxException;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;

public class AntenaUtil {

	/**
	 * Mtodo para dar permisso  pasta, necessita do comando su
	 * @param path pasta para qual ser dada permisso (Ex.: /dev)
	 * @param permission permisso que ser dada  pasta (Ex.: 777)
	 */
	public void changePermission(String path, int permission) {
		String command = "chmod -R " + permission + " " + path;
		try {
			CommandCapture commandCapture = new CommandCapture(0, command);
			RootTools.getShell(true).add(commandCapture).waitForFinish();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	/**
	 * Abre e inicializa o dispositivo, aps isso inicia o listen de leitura
	 * @param baudRate valor baud definido para o dispositivo
	 */
	public D2xx initDevice(int baudRate) throws D2xxException, Exception {
		D2xx ftD2xx = new D2xx();
		// open our first device
		ftD2xx.openByIndex(0);
		// configure our port
		// reset to UART mode for 232 devices
		ftD2xx.setBitMode((byte)0, D2xx.FT_BITMODE_RESET);

		// set baud rate
		ftD2xx.setBaudRate(baudRate);

		// set 8 data bits, 1 stop bit, no parity
		ftD2xx.setDataCharacteristics(D2xx.FT_DATA_BITS_8, D2xx.FT_STOP_BITS_1, D2xx.FT_PARITY_NONE);

		// set no flow control
		ftD2xx.setFlowControl(D2xx.FT_FLOW_NONE, (byte)0x11, (byte)0x13);

		// set latency timer to 16ms					
		ftD2xx.setLatencyTimer((byte)16);

		// set a read timeout
		ftD2xx.setTimeouts(10, 0);

		// purge buffers
		ftD2xx.purge((byte) (D2xx.FT_PURGE_TX | D2xx.FT_PURGE_RX));

		return ftD2xx;
	}

	public void sendCommand(D2xx ftD2xx, String command) throws Exception {
		ftD2xx.purge((byte) (D2xx.FT_PURGE_TX | D2xx.FT_PURGE_RX));
		
		Log.i("TESTE-JOHN", "**** COMMAND SENT: [" + command + "]");
		byte[] OutCommand = command.getBytes();
		try {
			//Escreve na antena o comando
			ftD2xx.write(OutCommand, command.length());
		} catch (D2xxException exception) {
			exception.printStackTrace();
		}
	}
}