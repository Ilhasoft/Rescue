#include <jni.h>
#include <android/log.h>
#include <stdio.h>

#include "com_ftdi_D2xx.h"
#include "com_ftdi_D2xx_FtDeviceInfoListNode.h"
#include "com_ftdi_D2xx_FtProgramData.h"

#include "ftd2xx.h"
#include "WinTypes.h"


#define DEBUG 0

#if DEBUG
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, "ftd2xx-jni",__VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , "ftd2xx-jni",__VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO   , "ftd2xx-jni",__VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN   , "ftd2xx-jni",__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR  , "ftd2xx-jni",__VA_ARGS__)
#else
#define LOGV(...) do{}while(0)
#define LOGD(...) do{}while(0)
#define LOGI(...) do{}while(0)
#define LOGW(...) do{}while(0)
#define LOGE(...) do{}while(0)
#endif



// FT_STATUS messages
static char * ftStatusExplanation[] = {
	"FT_OK",
	"FT_INVALID_HANDLE",
	"FT_DEVICE_NOT_FOUND",
	"FT_DEVICE_NOT_OPENED",
	"FT_IO_ERROR",
	"FT_INSUFFICIENT_RESOURCES",
	"FT_INVALID_PARAMETER",
	"FT_INVALID_BAUD_RATE",
	"FT_DEVICE_NOT_OPENED_FOR_ERASE",
	"FT_DEVICE_NOT_OPENED_FOR_WRITE",
	"FT_FAILED_TO_WRITE_DEVICE",
	"FT_EEPROM_READ_FAILED",
	"FT_EEPROM_WRITE_FAILED",
	"FT_EEPROM_ERASE_FAILED",
	"FT_EEPROM_NOT_PRESENT",
	"FT_EEPROM_NOT_PROGRAMMED",
	"FT_INVALID_ARGS",
	"FT_NOT_SUPPORTED",
	"FT_OTHER_ERROR"
};




// Globals
jfieldID ftHandleId;
jfieldID ftEventHandleId;




/**********************************************************************************
 *
 * Helper functions
 *
 **********************************************************************************/

void throwD2xxException(JNIEnv * env, jint d2xxStatusCode, const char * d2xxFunctionName)
{
	jclass d2xxExceptionClass;
	char exceptionBuffer[128];

	d2xxExceptionClass = env->FindClass("com/ftdi/D2xx$D2xxException");
	if (d2xxExceptionClass == 0)
	{
		LOGI("Failed to find D2XX exception class");
		return;
	}

	// build our exception message
	sprintf(exceptionBuffer, "Exception %s in function %s", ftStatusExplanation[d2xxStatusCode], d2xxFunctionName);

	if (env->ThrowNew(d2xxExceptionClass, (const char *)exceptionBuffer) == 0)
	{
		LOGI("D2XX exception thrown (%s)", exceptionBuffer);
	}
	else
	{
		LOGI("Failed to throw D2XX exception");
	}

	env->DeleteLocalRef(d2xxExceptionClass);

	return;
}




jint getNativeHandle(JNIEnv * env, jobject obj)
{
	return env->GetIntField(obj,ftHandleId);
}




void setNativeHandle(JNIEnv * env, jobject obj, jint handle)
{
	env->SetIntField(obj, ftHandleId, handle);
	return;
}




// helper function to map the FT_DEVICE_LIST_INFO_NODE data in to the corresponding Java class
jobject mapDeviceInfo(JNIEnv * env, FT_DEVICE_LIST_INFO_NODE * devNode)
{
	jclass devInfoClass;
	jobject devInfo;
	jfieldID devInfoField;
	jstring devString;

	// OK, got information on this device; populate our Java class
	devInfoClass = env->FindClass("com/ftdi/D2xx$FtDeviceInfoListNode");
	if (devInfoClass == 0)
	{
		LOGI("Failed to find device info class");
		return NULL;
	}

	devInfo = env->AllocObject(devInfoClass);
	if (devInfo == 0)
	{
		LOGI("Failed to allocate device info object");
		return NULL;
	}

	LOGI("Mapping data:");
	LOGI("  Flags = %x",devNode->Flags);
	LOGI("  Type = %d",devNode->Type);
	LOGI("  ID = %x",devNode->ID);
	LOGI("  LocId = %x",devNode->LocId);
	LOGI("  SerialNumber = %s",devNode->SerialNumber);
	LOGI("  Description = %s",devNode->Description);
	LOGI("  FTHandle = %x",devNode->ftHandle);

	// map data to fields in Java class
	devInfoField = env->GetFieldID(devInfoClass, "flags", "I");
	env->SetIntField(devInfo, devInfoField, devNode->Flags);

	devInfoField = env->GetFieldID(devInfoClass, "type", "I");
	env->SetIntField(devInfo, devInfoField, devNode->Type);

	devInfoField = env->GetFieldID(devInfoClass, "id", "I");
	env->SetIntField(devInfo, devInfoField, devNode->ID);

	devInfoField = env->GetFieldID(devInfoClass, "location","I");
	env->SetIntField(devInfo, devInfoField, devNode->LocId);

	devInfoField = env->GetFieldID(devInfoClass, "serialNumber", "Ljava/lang/String;");
	devString = env->NewStringUTF(devNode->SerialNumber);
	env->SetObjectField(devInfo, devInfoField, devString);

	devInfoField = env->GetFieldID(devInfoClass, "description", "Ljava/lang/String;");
	devString = env->NewStringUTF(devNode->Description);
	env->SetObjectField(devInfo, devInfoField, devString);

	devInfoField = env->GetFieldID(devInfoClass, "handle", "I");
	env->SetIntField(devInfo, devInfoField, (int)devNode->ftHandle);

	LOGI("Data mapped");

	env->DeleteLocalRef(devInfoClass);

	return devInfo;
}




jint getNativeEventHandle(JNIEnv * env, jobject obj)
{
	return env->GetIntField(obj,ftEventHandleId);
}




void setNativeEventHandle(JNIEnv * env, jobject obj, jint eventHandle)
{
	env->SetIntField(obj, ftEventHandleId, eventHandle);
	return;
}




/**********************************************************************************
 *
 * D2XX Native Functions
 *
 **********************************************************************************/

JNIEXPORT void JNICALL Java_com_ftdi_D2xx_setVIDPID (JNIEnv * env, jclass clazz, jint vid, jint pid)
{
	FT_STATUS status;

	LOGI("> FT_SetVIDPID (VID = 0x%x, PID = 0x%x)", (DWORD)vid, (DWORD)pid);
	status = FT_SetVIDPID((DWORD)vid, (DWORD)pid);
	LOGI("< FT_SetVIDPID (status = %d)", status);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_SetVIDPID");
	}

	return;
}




JNIEXPORT jintArray JNICALL Java_com_ftdi_D2xx_getVIDPID (JNIEnv * env, jclass clazz)
{
	FT_STATUS status;
	DWORD ids[2];
	jintArray result = env->NewIntArray(2);

	LOGI("> FT_GetVIDPID");
	status = FT_GetVIDPID(&ids[0], &ids[1]);
	LOGI("< FT_GetVIDPID (VID = 0x%x, PID = 0x%x), (status = %d)", ids[0], ids[1], status);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_GetVIDPID");
		return result;
	}

	// Now copy the IDs in to our Java array
	env->SetIntArrayRegion(result, 0, 2, (jint*)ids);

	return result;
}




JNIEXPORT jint JNICALL Java_com_ftdi_D2xx_createDeviceInfoList (JNIEnv * env, jclass clazz)
{
	FT_STATUS status;
	DWORD numDevs = 0;

	LOGI("> FT_CreateDeviceInfoList");
	status = FT_CreateDeviceInfoList(&numDevs);
	LOGI("< FT_CreateDeviceInfoList (numDevs = %d), (status = %d)", numDevs, status);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_CreateDeviceInfoList");
	}

	return (jint)numDevs;
}




JNIEXPORT jint JNICALL Java_com_ftdi_D2xx_getDeviceInfoList (JNIEnv * env, jclass clazz, jint numDevs, jobjectArray deviceList)
{
	FT_STATUS status;
	FT_DEVICE_LIST_INFO_NODE * devInfoList;
	DWORD numberDevices = 0;

	int i = 0;

	if (numDevs == 0)
		return 0;

	devInfoList = (FT_DEVICE_LIST_INFO_NODE*)malloc(sizeof(FT_DEVICE_LIST_INFO_NODE)*(DWORD)numDevs);

	if (devInfoList == NULL)
	{
		// no memory
		return 0;
	}

	// get the device information list
	LOGI("> FT_GetDeviceInfoList");
	status = FT_GetDeviceInfoList(devInfoList,&numberDevices);
	LOGI("< FT_GetDeviceInfoList (numberDevices = %d), (status = %d)", numberDevices, status);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_GetDeviceInfoList");
	}

	for (i =0; i < numberDevices; i++)
	{
		// map each element to our Java list
		env->SetObjectArrayElement(deviceList,i,mapDeviceInfo(env,&devInfoList[i]));

		LOGI("Information for device index %d\n",i);
		LOGI("  Flags = 0x%x",devInfoList[i].Flags);
		LOGI("  Type = 0x%x",devInfoList[i].Type);
		LOGI("  ID = 0x%x",devInfoList[i].ID);
		LOGI("  Location = 0x%x",devInfoList[i].LocId);
		LOGI("  Serial Number = %s",devInfoList[i].SerialNumber);
		LOGI("  Description = %s",devInfoList[i].Description);
		LOGI("  Handle = 0x%x",devInfoList[i].ftHandle);

	}

	free(devInfoList);
	devInfoList = NULL;

	return numberDevices;
}




JNIEXPORT jobject JNICALL Java_com_ftdi_D2xx_getDeviceInfoListDetail (JNIEnv * env, jclass clazz, jint index)
{
	FT_STATUS status;
	FT_DEVICE_LIST_INFO_NODE devNode;

	LOGI("> FT_GetDeviceInfoListDetail");
	status = FT_GetDeviceInfoDetail(index, &devNode.Flags, &devNode.Type, &devNode.ID, &devNode.LocId, devNode.SerialNumber, devNode.Description, &devNode.ftHandle);
	LOGI("Information for device index %d\n",index);
	LOGI("  Flags = 0x%x",devNode.Flags);
	LOGI("  Type = 0x%x",devNode.Type);
	LOGI("  ID = 0x%x",devNode.ID);
	LOGI("  Location = 0x%x",devNode.LocId);
	LOGI("  Serial Number = %s",devNode.SerialNumber);
	LOGI("  Description = %s",devNode.Description);
	LOGI("  Handle = 0x%x",devNode.ftHandle);
	LOGI("< FT_GetDeviceInfoListDetail (status = %d)",status);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_GetDeviceInfoDetail");
	}

	// use our helper function to map the information to our Java class
	return mapDeviceInfo(env, &devNode);
}




JNIEXPORT jint JNICALL Java_com_ftdi_D2xx_getLibraryVersion (JNIEnv * env, jclass clazz)
{
	FT_STATUS status;
	DWORD libVer = 0;

	LOGI("> FT_GetLibraryVersion");
	status = FT_GetLibraryVersion(&libVer);
	LOGI("< FT_GetDriverVersion (libVer = 0x%x), (status = %d)", libVer, status);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_GetLibraryVersion");
	}

	return (jint)libVer;
}




JNIEXPORT void JNICALL Java_com_ftdi_D2xx_openByIndex (JNIEnv * env, jobject obj, jint index)
{
	FT_STATUS status;
	FT_HANDLE handle;

	handle = (FT_HANDLE)getNativeHandle(env, obj);
	if (handle != 0)
	{
		// this instance already has a device open - do not allow anotehr open to happen
		// throw an exception
	}

	LOGI("> FT_Open");
	status = FT_Open((DWORD)index,&handle);
	LOGI("  Handle = 0x%x",handle);
	LOGI("< FT_Open (status = %d)",status);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_Open");
		return;
	}

	// update our reference to the native handle - used for all subsequent IO
	setNativeHandle(env,obj,(jint)handle);

	return;
}




JNIEXPORT void JNICALL Java_com_ftdi_D2xx_openBySerialNumber (JNIEnv * env, jobject obj, jstring serialNumber)
{
	FT_STATUS status;
	FT_HANDLE handle;

	const char * serialStr;

	handle = (FT_HANDLE)getNativeHandle(env, obj);
	if (handle != 0)
	{
		// this instance already has a device open - do not allow anotehr open to happen
		// throw an exception
	}

	serialStr = env->GetStringUTFChars(serialNumber, 0);

	LOGI("> FT_OpenEx - serial number (%s)",serialStr);
	status = FT_OpenEx((void*)serialStr,FT_OPEN_BY_SERIAL_NUMBER,&handle);
	LOGI("  Handle = 0x%x",handle);
	LOGI("< FT_OpenEx - serial number (status = %d)",status);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_OpenEx (serial number)");
		return;
	}

	// update our reference to the native handle - used for all subsequent IO
	setNativeHandle(env,obj,(jint)handle);

	return;
}




JNIEXPORT void JNICALL Java_com_ftdi_D2xx_openByDescription (JNIEnv * env, jobject obj, jstring description)
{
	FT_STATUS status;
	FT_HANDLE handle;

	const char * descStr;

	handle = (FT_HANDLE)getNativeHandle(env, obj);
	if (handle != 0)
	{
		// this instance already has a device open - do not allow anotehr open to happen
		// throw an exception
	}

	descStr = env->GetStringUTFChars(description, 0);

	LOGI("> FT_OpenEx - description (%s)",descStr);
	status = FT_OpenEx((void*)descStr,FT_OPEN_BY_DESCRIPTION,&handle);
	LOGI("  Handle = 0x%x",handle);
	LOGI("< FT_OpenEx - description (status = %d)",status);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_OpenEx (description)");
		return;
	}

	// update our reference to the native handle - used for all subsequent IO
	setNativeHandle(env,obj,(jint)handle);

	return;
}




JNIEXPORT void JNICALL Java_com_ftdi_D2xx_openByLocation (JNIEnv * env, jobject obj, jint location)
{
	FT_STATUS status;
	FT_HANDLE handle;

	handle = (FT_HANDLE)getNativeHandle(env, obj);
	if (handle != 0)
	{
		// this instance already has a device open - do not allow anotehr open to happen
		// throw an exception
	}

	LOGI("> FT_OpenEx - location (0x%x)", (DWORD)location);
	status = FT_OpenEx((void*)location,FT_OPEN_BY_LOCATION,&handle);
	LOGI("  Handle = 0x%x",handle);
	LOGI("< FT_OpenEx - location (status = %d)",status);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_OpenEx (location)");
		return;
	}

	// update our reference to the native handle - used for all subsequent IO
	setNativeHandle(env,obj,(jint)handle);

	return;
}




JNIEXPORT void JNICALL Java_com_ftdi_D2xx_close (JNIEnv * env, jobject obj)
{
	FT_STATUS status;
	FT_HANDLE handle;

	int eventHandle;
	EVENT_HANDLE * eh;

	handle = (FT_HANDLE)getNativeHandle(env, obj);

	LOGI("> FT_Close");
	status = FT_Close(handle);
	LOGI("< FT_Close (status = %d)",status);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_Close");
		return;
	}

	// reset ftHandle value to default value of 0
	setNativeHandle(env, obj, 0);

	// if we have an outstanding event handle allocation, free it
	eventHandle = getNativeEventHandle(env, obj);

	if (eventHandle != 0)
	{
		LOGI("Freeing event handle");
		eh = (EVENT_HANDLE*)eventHandle;
		free(eh);
	}

	// reset ftEventHandle value to default value of 0
	setNativeEventHandle(env, obj, 0);

	return;
}




JNIEXPORT jint JNICALL Java_com_ftdi_D2xx_read (JNIEnv * env, jobject obj, jbyteArray data, jint count)
{
	FT_STATUS status;
	FT_HANDLE handle;

	jbyte * buffer;
	DWORD numRead = 0;

	handle = (FT_HANDLE)getNativeHandle(env, obj);

	buffer = env->GetByteArrayElements(data, 0);

	LOGI("> FT_Read (%d bytes)",(DWORD)count);
	status = FT_Read(handle, (void*)buffer, (DWORD)count, &numRead);
	LOGI("< FT_Read (numRead = %d),(status = %d)", numRead, status);

	env->ReleaseByteArrayElements(data, buffer, 0);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_Read");
	}

	return (jint)numRead;
}




JNIEXPORT jint JNICALL Java_com_ftdi_D2xx_write (JNIEnv * env, jobject obj, jbyteArray data, jint count)
{
	FT_STATUS status;
	FT_HANDLE handle;

	jbyte * buffer;
	DWORD numWritten = 0;

	handle = (FT_HANDLE)getNativeHandle(env, obj);

	buffer = env->GetByteArrayElements(data, 0);

	LOGI("> FT_Write (%d bytes)",(DWORD)count);
	status = FT_Write(handle, (void*)buffer, (DWORD)count, &numWritten);
	LOGI("< FT_Write (numWritten = %d),(status = %d)", numWritten, status);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_Write");
	}

	env->ReleaseByteArrayElements(data, buffer, 0);

	return (jint)numWritten;
}




JNIEXPORT void JNICALL Java_com_ftdi_D2xx_setBaudRate (JNIEnv * env, jobject obj, jint baudRate)
{
	FT_STATUS status;
	FT_HANDLE handle;

	handle = (FT_HANDLE)getNativeHandle(env,obj);

	LOGI("> FT_SetBaudRate (baud rate = %d)", (DWORD)baudRate);
	status = FT_SetBaudRate(handle, (DWORD)baudRate);
	LOGI("< FT_SetBaudRate (status = %d)", status);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_SetBaudRate");
	}

	return;
}




JNIEXPORT void JNICALL Java_com_ftdi_D2xx_setDataCharacteristics (JNIEnv * env, jobject obj, jbyte dataBits, jbyte stopBits, jbyte parity)
{
	FT_STATUS status;
	FT_HANDLE handle;

	handle = (FT_HANDLE)getNativeHandle(env,obj);

	LOGI("> FT_SetDataCharacteristics (data bits = %d, stop bits = % d, parity = %d)", (UCHAR)dataBits, (UCHAR)stopBits, (UCHAR)parity);
	status = FT_SetDataCharacteristics(handle, (UCHAR)dataBits, (UCHAR)stopBits, (UCHAR)parity);
	LOGI("< FT_SetDataCharacteristics (status = %d)", status);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_SetDataCharacteristics");
	}

	return;
}




JNIEXPORT void JNICALL Java_com_ftdi_D2xx_setFlowControl (JNIEnv * env, jobject obj, jshort flowControl, jbyte xon, jbyte xoff)
{
	FT_STATUS status;
	FT_HANDLE handle;

	handle = (FT_HANDLE)getNativeHandle(env,obj);

	LOGI("> FT_SetFlowControl (flow control = 0x%x, xon = 0x%x, xoff = 0x%x)", (WORD)flowControl, (UCHAR)xon, (UCHAR)xoff);
	status = FT_SetFlowControl(handle, (WORD)flowControl, (UCHAR)xon, (UCHAR)xoff);
	LOGI("< FT_SetFlowControl (status = %d)", status);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_SetFlowControl");
	}

	return;
}




JNIEXPORT void JNICALL Java_com_ftdi_D2xx_setTimeouts (JNIEnv * env, jobject obj, jint readTimeout, jint writeTimeout)
{
	FT_STATUS status;
	FT_HANDLE handle;

	handle = (FT_HANDLE)getNativeHandle(env,obj);

	LOGI("> FT_SetTimeouts (read timeout = %d, write timeout = %d)", (DWORD)readTimeout, (DWORD)writeTimeout);
	status = FT_SetTimeouts(handle, (DWORD)readTimeout, (DWORD)writeTimeout);
	LOGI("< FT_SetTimeouts (status = %d)", status);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_SetTimeouts");
	}

	return;
}




JNIEXPORT void JNICALL Java_com_ftdi_D2xx_setDtr (JNIEnv * env, jobject obj)
{
	FT_STATUS status;
	FT_HANDLE handle;

	handle = (FT_HANDLE)getNativeHandle(env,obj);

	LOGI("> FT_SetDtr");
	status = FT_SetDtr(handle);
	LOGI("< FT_SetDtr (status = %d)", status);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_SetDtr");
	}

	return;
}




JNIEXPORT void JNICALL Java_com_ftdi_D2xx_clrDtr (JNIEnv * env, jobject obj)
{
	FT_STATUS status;
	FT_HANDLE handle;

	handle = (FT_HANDLE)getNativeHandle(env,obj);

	LOGI("> FT_ClrDtr");
	status = FT_ClrDtr(handle);
	LOGI("< FT_ClrDtr (status = %d)", status);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_ClrDtr");
	}

	return;
}




JNIEXPORT void JNICALL Java_com_ftdi_D2xx_setRts (JNIEnv * env, jobject obj)
{
	FT_STATUS status;
	FT_HANDLE handle;

	handle = (FT_HANDLE)getNativeHandle(env,obj);

	LOGI("> FT_SetRts");
	status = FT_SetRts(handle);
	LOGI("< FT_SetRts (status = %d)", status);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_SetRts");
	}

	return;
}




JNIEXPORT void JNICALL Java_com_ftdi_D2xx_clrRts (JNIEnv * env, jobject obj)
{
	FT_STATUS status;
	FT_HANDLE handle;

	handle = (FT_HANDLE)getNativeHandle(env,obj);

	LOGI("> FT_ClrRts");
	status = FT_ClrRts(handle);
	LOGI("< FT_ClrRts (status = %d)", status);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_ClrRts");
	}

	return;
}




JNIEXPORT void JNICALL Java_com_ftdi_D2xx_setBreakOn (JNIEnv * env, jobject obj)
{
	FT_STATUS status;
	FT_HANDLE handle;

	handle = (FT_HANDLE)getNativeHandle(env,obj);

	LOGI("> FT_SetBreakOn");
	status = FT_SetBreakOn(handle);
	LOGI("< FT_SetBreakOn (status = %d)", status);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_SetBreakOn");
	}

	return;
}




JNIEXPORT void JNICALL Java_com_ftdi_D2xx_setBreakOff (JNIEnv * env, jobject obj)
{
	FT_STATUS status;
	FT_HANDLE handle;

	handle = (FT_HANDLE)getNativeHandle(env,obj);

	LOGI("> FT_SetBreakOff");
	status = FT_SetBreakOff(handle);
	LOGI("< FT_SetBreakOff (status = %d)", status);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_SetBreakOff");
	}

	return;
}




JNIEXPORT jbyteArray JNICALL Java_com_ftdi_D2xx_getModemStatus (JNIEnv * env, jobject obj)
{
	FT_STATUS status;
	FT_HANDLE handle;
	DWORD modemStatus;
	UCHAR modemLineStatus[2];
	jbyteArray result = env->NewByteArray(2);

	handle = (FT_HANDLE)getNativeHandle(env,obj);

	LOGI("> FT_GetModemStatus");
	status = FT_GetModemStatus(handle, &modemStatus);
	LOGI("< FT_GetModemStatus (modemStatus = 0x%x), (status = %d)", modemStatus, status);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_GetModemStatus");
		return result;
	}

	// Modem status is in the least significant byte, Line status is in the next byte
	modemLineStatus[0] = (UCHAR)(modemStatus & 0x000000FF);
	modemLineStatus[1] = (UCHAR)((modemStatus >> 8) & 0x000000FF);

	// Now copy the status values in to our Java array
	env->SetByteArrayRegion(result, 0, 2, (jbyte*)modemLineStatus);

	return result;
}




JNIEXPORT jint JNICALL Java_com_ftdi_D2xx_getQueueStatus (JNIEnv * env, jobject obj)
{
	FT_STATUS status;
	FT_HANDLE handle;
	DWORD qStat;

	handle = (FT_HANDLE)getNativeHandle(env,obj);

	LOGI("> FT_GetQueueStatus");
	status = FT_GetQueueStatus(handle, &qStat);
	LOGI("< FT_GetQueueStatus (qStat = 0x%x), (status = %d)", qStat, status);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_GetQueueStatus");
	}

	return (jint)qStat;
}




JNIEXPORT jintArray JNICALL Java_com_ftdi_D2xx_getStatus (JNIEnv * env, jobject obj)
{
	FT_STATUS status;
	FT_HANDLE handle;
	DWORD rxQStatus, txQStatus, eventStatus;
	DWORD devStatus[3];

	jintArray result = env->NewIntArray(3);

	handle = (FT_HANDLE)getNativeHandle(env,obj);

	LOGI("> FT_GetStatus");
	status = FT_GetStatus(handle, &rxQStatus, &txQStatus, &eventStatus);
	LOGI("< FT_GetStatus (rxQStatus = %d), (txQStatus = %d), (eventStatus = %d), (status = %d)", rxQStatus, txQStatus, eventStatus, status);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_GetStatus");
		return result;
	}

	// Modem status is in the least significant byte, Line status is in the next byte
	devStatus[0] = (DWORD)rxQStatus;
	devStatus[1] = (DWORD)txQStatus;
	devStatus[1] = (DWORD)eventStatus;

	// Now copy the status values in to our Java array
	env->SetIntArrayRegion(result, 0, 3, (jint*)devStatus);

	return result;
}




JNIEXPORT void JNICALL Java_com_ftdi_D2xx_purge (JNIEnv * env, jobject obj, jbyte flags)
{
	FT_STATUS status;
	FT_HANDLE handle;

	handle = (FT_HANDLE)getNativeHandle(env,obj);

	LOGI("> FT_Purge (flags = 0x%x)",(DWORD)flags);
	status = FT_Purge(handle, (DWORD)flags);
	LOGI("< FT_Purge (status = %d)", status);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_Purge");
	}

	return;
}




JNIEXPORT void JNICALL Java_com_ftdi_D2xx_resetDevice (JNIEnv * env, jobject obj)
{
	FT_STATUS status;
	FT_HANDLE handle;

	handle = (FT_HANDLE)getNativeHandle(env,obj);

	LOGI("> FT_ResetDevice");
	status = FT_ResetDevice(handle);
	LOGI("< FT_ResetDevice (status = %d)", status);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_ResetDevice");
	}

	return;
}




JNIEXPORT void JNICALL Java_com_ftdi_D2xx_stopInTask (JNIEnv * env, jobject obj)
{
	FT_STATUS status;
	FT_HANDLE handle;

	handle = (FT_HANDLE)getNativeHandle(env,obj);

	LOGI("> FT_StopInTask");
	status = FT_StopInTask(handle);
	LOGI("< FT_StopInTask (status = %d)", status);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_StopInTask");
	}

	return;
}




JNIEXPORT void JNICALL Java_com_ftdi_D2xx_restartInTask (JNIEnv * env, jobject obj)
{
	FT_STATUS status;
	FT_HANDLE handle;

	handle = (FT_HANDLE)getNativeHandle(env,obj);

	LOGI("> FT_RestartInTask");
	status = FT_RestartInTask(handle);
	LOGI("< FT_RestartInTask (status = %d)", status);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_RestartInTask");
	}

	return;
}




JNIEXPORT jobject JNICALL Java_com_ftdi_D2xx_getDeviceInfo (JNIEnv * env, jobject obj)
{
	FT_STATUS status;
	FT_HANDLE handle;

	DWORD deviceType;
	DWORD deviceID;
	char SerialNumber[16];
	char Description[64];

	jclass devInfoClass;
	jobject devInfo;
	jfieldID devInfoField;
	jstring devString;

	handle = (FT_HANDLE)getNativeHandle(env,obj);

	LOGI("> FT_GetDeviceInfo");
	status = FT_GetDeviceInfo(handle, &deviceType, &deviceID, SerialNumber, Description, NULL);
	LOGI("Information for device\n");
	LOGI("  Type = 0x%x",deviceType);
	LOGI("  ID = 0x%x",deviceID);
	LOGI("  Serial Number = %s",SerialNumber);
	LOGI("  Description = %s",Description);
	LOGI("< FT_GetDeviceInfo (status = %d)",status);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_GetDeviceInfo");
		return NULL;
	}

	// OK, got information on this device; populate our Java class
	devInfoClass = env->FindClass("com/ftdi/D2xx$FtDeviceInfoListNode");
	if (devInfoClass == 0)
	{
		LOGI("Failed to find class");
		return NULL;
	}

	devInfo = env->AllocObject(devInfoClass);
	if (devInfo == 0)
	{
		LOGI("Failed to allocate object");
		return NULL;
	}

	LOGI("Mapping data...");
	// map data to fields in Java class
	devInfoField = env->GetFieldID(devInfoClass, "flags", "I");
	env->SetIntField(devInfo, devInfoField, 0);

	devInfoField = env->GetFieldID(devInfoClass, "type", "I");
	env->SetIntField(devInfo, devInfoField, deviceType);

	devInfoField = env->GetFieldID(devInfoClass, "id", "I");
	env->SetIntField(devInfo, devInfoField, deviceID);

	devInfoField = env->GetFieldID(devInfoClass, "location","I");
	env->SetIntField(devInfo, devInfoField, 0);

	devInfoField = env->GetFieldID(devInfoClass, "serialNumber", "Ljava/lang/String;");
	devString = env->NewStringUTF(SerialNumber);
	env->SetObjectField(devInfo, devInfoField, devString);

	devInfoField = env->GetFieldID(devInfoClass, "description", "Ljava/lang/String;");
	devString = env->NewStringUTF(Description);
	env->SetObjectField(devInfo, devInfoField, devString);

	devInfoField = env->GetFieldID(devInfoClass, "handle", "I");
	env->SetIntField(devInfo, devInfoField, (int)handle);

	LOGI("Data mapped");

	env->DeleteLocalRef(devInfoClass);

	return devInfo;
}




JNIEXPORT void JNICALL Java_com_ftdi_D2xx_setChars (JNIEnv * env, jobject obj, jbyte eventChar, jbyte eventCharEnable, jbyte errorChar, jbyte errorCharEnable)
{
	FT_STATUS status;
	FT_HANDLE handle;

	handle = (FT_HANDLE)getNativeHandle(env,obj);

	LOGI("> FT_SetChars (event char = 0x%x, event char enable = 0x%x, error char = 0x%x, error char enable = 0x%x)",(UCHAR)eventChar,(UCHAR)eventCharEnable,(UCHAR)errorChar,(UCHAR)errorCharEnable);
	status = FT_SetChars(handle, (UCHAR)eventChar,(UCHAR)eventCharEnable,(UCHAR)errorChar,(UCHAR)errorCharEnable);
	LOGI("< FT_SetChars (status = %d)", status);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_SetChars");
	}

	return;
}




JNIEXPORT void JNICALL Java_com_ftdi_D2xx_setUSBParameters (JNIEnv * env, jobject obj, jint inTransferSize, jint outTransferSize)
{
	FT_STATUS status;
	FT_HANDLE handle;

	handle = (FT_HANDLE)getNativeHandle(env,obj);

	LOGI("> FT_SetUSBParameters (in transfer size = 0x%x, out transfer size = 0x%x)",(DWORD)inTransferSize, (DWORD)outTransferSize);
	status = FT_SetUSBParameters(handle, (DWORD)inTransferSize, (DWORD)outTransferSize);
	LOGI("< FT_SetUSBParameters (status = %d)", status);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_SetUSBParameters");
	}

	return;
}




JNIEXPORT jint JNICALL Java_com_ftdi_D2xx_getDriverVersion (JNIEnv * env, jobject obj)
{
	FT_STATUS status;
	FT_HANDLE handle;
	DWORD driverVer = 0;

	handle = (FT_HANDLE)getNativeHandle(env,obj);

	LOGI("> FT_GetDriverVersion");
	status = FT_GetDriverVersion(handle, &driverVer);
	LOGI("< FT_GetDriverVersion (driverVer = 0x%x), (status = %d)", driverVer, status);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_GetDriverVersion");
	}

	return (jint)driverVer;
}




JNIEXPORT void JNICALL Java_com_ftdi_D2xx_setEventNotification (JNIEnv * env, jobject obj, jint eventMask)
{
	FT_STATUS status;
	FT_HANDLE handle;
	int eventHandle;
	EVENT_HANDLE * eh;


	eventHandle = getNativeEventHandle(env, obj);

	if (eventHandle != 0)
	{
		// we have been here before; recycle our event structure
		LOGI("Using old event handle");
		eh = (EVENT_HANDLE*)eventHandle;
	}
	else
	{
		LOGI("Allocating new event handle");
		// not been here yet, need to malloc an event handle
		eh = (EVENT_HANDLE*)malloc(sizeof(EVENT_HANDLE));
		if (eh == NULL)
		{
			// failed - throw an exception here
			throwD2xxException(env, FT_INSUFFICIENT_RESOURCES, "FT_SetEventNotification");
			return;
		}

		// store our event handle
		setNativeEventHandle(env, obj,(int)eh);
	}

	// initialise the event
	pthread_mutex_init(&eh->eMutex, NULL);
	pthread_cond_init(&eh->eCondVar, NULL);

	// set our notification
	handle = (FT_HANDLE)getNativeHandle(env,obj);

	LOGI("> FT_SetEventNotification (event mask = 0x%x)", (DWORD)eventMask);
	status = FT_SetEventNotification(handle, (DWORD)eventMask, (PVOID)eh);
	LOGI("< FT_SetEventNotification (status = %d)", status);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_SetEventNotification");
	}

	return;
}




JNIEXPORT void JNICALL Java_com_ftdi_D2xx_waitEvent (JNIEnv * env, jobject obj)
{
	int eventHandle;
	EVENT_HANDLE * eh;

	LOGI("> WaitEvent");
	eventHandle = getNativeEventHandle(env, obj);

	if (eventHandle == 0)
	{
		// not set event notification - don't do anything, maybe an exception?

	}

	eh = (EVENT_HANDLE*)eventHandle;

	pthread_mutex_lock(&eh->eMutex);
	pthread_cond_wait(&eh->eCondVar, &eh->eMutex);
	pthread_mutex_unlock(&eh->eMutex);

	LOGI("< WaitEvent");
	return;
}




JNIEXPORT jboolean JNICALL Java_com_ftdi_D2xx_waitEventTimed (JNIEnv * env, jobject obj , jint waittimeout)
{
	int eventHandle;
	EVENT_HANDLE * eh;

	int waitResult;
	struct timeval now;
	struct timespec timeout;

	LOGI("> WaitEventTimed (timeout = %dms)",waittimeout);
	eventHandle = getNativeEventHandle(env, obj);

	if (eventHandle == 0)
	{
		// not set event notification - don't do anything, maybe an exception?

	}

	gettimeofday(&now, NULL);
	timeout.tv_sec = now.tv_sec + (waittimeout / 1000);

	// assign us value here for now...
	timeout.tv_nsec = now.tv_usec + ((waittimeout % 1000) * 1000);

	// check for being greater than 1s
	if (timeout.tv_nsec >= 1000000)
	{
		timeout.tv_sec++;
		timeout.tv_nsec -= 1000000;
	}

	// OK , mulitiply up to ns value
	timeout.tv_nsec *= 1000;

	eh = (EVENT_HANDLE*)eventHandle;

	pthread_mutex_lock(&eh->eMutex);

	if (pthread_cond_timedwait(&eh->eCondVar, &eh->eMutex, &timeout) != 0)
	{
		// our wait timed out!
		// or error...
		LOGI("< WaitEventTimed timed out");
		pthread_mutex_unlock(&eh->eMutex);
		return false;
	}
	else
	{
		// our event fired
		LOGI("< WaitEventTimed got event");
		pthread_mutex_unlock(&eh->eMutex);
		return true;
	}

}




/**********************************************************************************
 *
 * EXTENDED FUNCTIONS - ONLY AVAILABLE OFR FT232B DEVICES AND LATER
 *
 **********************************************************************************/

JNIEXPORT void JNICALL Java_com_ftdi_D2xx_setLatencyTimer (JNIEnv * env, jobject obj, jbyte latency)
{
	FT_STATUS status;
	FT_HANDLE handle;

	handle = (FT_HANDLE)getNativeHandle(env,obj);

	LOGI("> FT_SetLatencyTimer (latency = %d)",(UCHAR)latency);
	status = FT_SetLatencyTimer(handle,(UCHAR)latency);
	LOGI("< FT_SetLatencyTimer (status = %d)", status);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_SetLatencyTimer");
	}

	return;
}




JNIEXPORT jbyte JNICALL Java_com_ftdi_D2xx_getLatencyTimer (JNIEnv * env, jobject obj)
{
	FT_STATUS status;
	FT_HANDLE handle;
	UCHAR latency;

	handle = (FT_HANDLE)getNativeHandle(env,obj);

	LOGI("> FT_GetLatencyTimer");
	status = FT_GetLatencyTimer(handle,&latency);
	LOGI("< FT_GetLatencyTimer (latency = %d), (status = %d)", latency, status);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_GetLatencyTimer");
	}

	return (jbyte)latency;
}




JNIEXPORT void JNICALL Java_com_ftdi_D2xx_setBitMode (JNIEnv * env, jobject obj, jbyte mask, jbyte bitMode)
{
	FT_STATUS status;
	FT_HANDLE handle;

	handle = (FT_HANDLE)getNativeHandle(env,obj);

	LOGI("> FT_SetBitMode (mask = 0x%x), (bitMode = 0x%x)",(UCHAR)mask, (UCHAR)bitMode);
	status = FT_SetBitMode(handle,(UCHAR)mask, (UCHAR)bitMode);
	LOGI("< FT_SetBitMode (status = %d)", status);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_SetBitMode");
	}

	return;
}




JNIEXPORT jbyte JNICALL Java_com_ftdi_D2xx_getPinStates (JNIEnv * env, jobject obj)
{
	FT_STATUS status;
	FT_HANDLE handle;
	UCHAR bitMode = 0;

	handle = (FT_HANDLE)getNativeHandle(env,obj);

	LOGI("> FT_GetBitMode");
	status = FT_GetBitMode(handle,&bitMode);
	LOGI("< FT_GetBitMode (bitMode = %d), (status = %d)", bitMode, status);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_GetBitMode");
	}

	return (jbyte)bitMode;
}




/**********************************************************************************
 *
 * EEPROM FUNCTIONS
 *
 **********************************************************************************/

JNIEXPORT jshort JNICALL Java_com_ftdi_D2xx_eepromReadWord (JNIEnv * env, jobject obj, jint address)
{
	FT_STATUS status;
	FT_HANDLE handle;
	USHORT data = 0;

	handle = (FT_HANDLE)getNativeHandle(env, obj);

	LOGI("> FT_ReadEE (address = 0x%x)", (DWORD)address);
	status = FT_ReadEE(handle,address,&data);
	LOGI("< FT_ReadEE (data = 0x%x), (status = %d)", data, status);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_ReadEE");
	}

	return (jshort)data;
}




JNIEXPORT void JNICALL Java_com_ftdi_D2xx_eepromWriteWord (JNIEnv * env, jobject obj, jint address, jshort data)
{
	FT_STATUS status;
	FT_HANDLE handle;

	handle = (FT_HANDLE)getNativeHandle(env, obj);

	LOGI("> FT_WriteEE (address = 0x%x), (data = 0x%x)", (DWORD)address, (USHORT)data);
	status = FT_WriteEE(handle,(DWORD)address,(USHORT)data);
	LOGI("< FT_WriteEE (status = %d)", status);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_WriteEE");
	}

	return;
}




JNIEXPORT void JNICALL Java_com_ftdi_D2xx_eepromErase (JNIEnv * env, jobject obj)
{
	FT_STATUS status;
	FT_HANDLE handle;

	handle = (FT_HANDLE)getNativeHandle(env, obj);

	LOGI("> FT_EraseEE");
	status = FT_EraseEE(handle);
	LOGI("< FT_EraseEE (status = %d)", status);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_EraseEE");
	}

	return;
}




JNIEXPORT jobject JNICALL Java_com_ftdi_D2xx_eepromRead (JNIEnv * env, jobject obj)
{
	FT_STATUS status;
	FT_HANDLE handle;

	FT_PROGRAM_DATA eepromData;

	char ManufacturerBuf[32];
	char ManufacturerIdBuf[16];
	char DescriptionBuf[64];
	char SerialNumberBuf[16];

	jclass devEepromClass;
	jobject devEeprom;
	jfieldID devEepromField;
	jstring devEepromString;


	handle = (FT_HANDLE)getNativeHandle(env, obj);

	// initialisation of structure
	eepromData.Signature1 = 0x00000000;
	eepromData.Signature2 = 0xFFFFFFFF;
	eepromData.Version = 5;	// use FT232H extensions
	eepromData.Manufacturer = ManufacturerBuf;
	eepromData.ManufacturerId = ManufacturerIdBuf;
	eepromData.Description = DescriptionBuf;
	eepromData.SerialNumber = SerialNumberBuf;

	LOGI("> FT_EE_Read");
	status = FT_EE_Read(handle, &eepromData);
	LOGI("< FT_EE_Read (status = %d)", status);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_EE_Read");
		return NULL;
	}

	// OK, got information on this device; populate our Java class
	devEepromClass = env->FindClass("com/ftdi/D2xx$FtProgramData");
	if (devEepromClass == 0)
	{
		LOGI("Failed to find EEPROM class");
		return NULL;
	}

	devEeprom = env->AllocObject(devEepromClass);
	if (devEeprom == 0)
	{
		LOGI("Failed to allocate EEPROM object");
		return NULL;
	}

	// map data to fields in Java class
// Common elements
	devEepromField = env->GetFieldID(devEepromClass, "vendorId", "S");
	env->SetShortField(devEeprom, devEepromField, eepromData.VendorId);

	devEepromField = env->GetFieldID(devEepromClass, "productId", "S");
	env->SetShortField(devEeprom, devEepromField, eepromData.ProductId);

	devEepromField = env->GetFieldID(devEepromClass, "manufacturer", "Ljava/lang/String;");
	devEepromString = env->NewStringUTF(eepromData.Manufacturer);
	if (devEepromString != NULL)
	{
		env->SetObjectField(devEeprom, devEepromField, devEepromString);
	}

	devEepromField = env->GetFieldID(devEepromClass, "manufacturerId", "Ljava/lang/String;");
	devEepromString = env->NewStringUTF(eepromData.ManufacturerId);
	if (devEepromString != NULL)
	{
		env->SetObjectField(devEeprom, devEepromField, devEepromString);
	}

	devEepromField = env->GetFieldID(devEepromClass, "description", "Ljava/lang/String;");
	devEepromString = env->NewStringUTF(eepromData.Description);
	if (devEepromString != NULL)
	{
		env->SetObjectField(devEeprom, devEepromField, devEepromString);
	}

	devEepromField = env->GetFieldID(devEepromClass, "serialNumber", "Ljava/lang/String;");
	devEepromString = env->NewStringUTF(eepromData.SerialNumber);
	if (devEepromString != NULL)
	{
		env->SetObjectField(devEeprom, devEepromField, devEepromString);
	}

	devEepromField = env->GetFieldID(devEepromClass, "maxPower", "S");
	env->SetShortField(devEeprom, devEepromField, eepromData.MaxPower);

	devEepromField = env->GetFieldID(devEepromClass, "selfPowered", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.SelfPowered == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "remoteWakeup", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.RemoteWakeup == 0 ? false:true));

// FT232B extensions
	devEepromField = env->GetFieldID(devEepromClass, "rev4", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.Rev4 == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "pullDownEnable", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.PullDownEnable == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "serNumEnable", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.SerNumEnable == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "USBVersionEnable", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.USBVersionEnable == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "USBVersion", "S");
	env->SetShortField(devEeprom, devEepromField, eepromData.USBVersion);

// FT2232 extensions - Version 1
	devEepromField = env->GetFieldID(devEepromClass, "rev5", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.Rev5 == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "pullDownEnable5", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.PullDownEnable5 == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "serNumEnable5", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.SerNumEnable5 == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "USBVersionEnable5", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.USBVersionEnable5 == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "USBVersion5", "S");
	env->SetShortField(devEeprom, devEepromField, eepromData.USBVersion5);

	devEepromField = env->GetFieldID(devEepromClass, "AIsHighCurrent", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.AIsHighCurrent == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "BIsHighCurrent", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.BIsHighCurrent == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "ifAIsFifo", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.IFAIsFifo == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "ifAIsFifoTar", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.IFAIsFifoTar == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "ifAIsFastSer", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.IFAIsFastSer == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "AIsVCP", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.AIsVCP == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "ifBIsFifo", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.IFBIsFifo == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "ifBIsFifoTar", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.IFBIsFifoTar == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "ifBIsFastSer", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.IFBIsFastSer == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "BIsVCP", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.BIsVCP == 0 ? false:true));

// FT232R extensions - Version 2
	devEepromField = env->GetFieldID(devEepromClass, "useExtOsc", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.UseExtOsc == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "highDriveIOs", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.HighDriveIOs == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "pullDownEnableR", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.PullDownEnableR == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "serNumEnableR", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.SerNumEnableR == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "invertTXD", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.InvertTXD == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "invertRXD", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.InvertRXD == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "invertRTS", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.InvertRTS == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "invertCTS", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.InvertCTS == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "invertDTR", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.InvertDTR == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "invertDSR", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.InvertDSR == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "invertDCD", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.InvertDCD == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "invertRI", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.InvertRI == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "cbus0", "B");
	env->SetByteField(devEeprom, devEepromField, eepromData.Cbus0);

	devEepromField = env->GetFieldID(devEepromClass, "cbus1", "B");
	env->SetByteField(devEeprom, devEepromField, eepromData.Cbus1);

	devEepromField = env->GetFieldID(devEepromClass, "cbus2", "B");
	env->SetByteField(devEeprom, devEepromField, eepromData.Cbus2);

	devEepromField = env->GetFieldID(devEepromClass, "cbus3", "B");
	env->SetByteField(devEeprom, devEepromField, eepromData.Cbus3);

	devEepromField = env->GetFieldID(devEepromClass, "cbus4", "B");
	env->SetByteField(devEeprom, devEepromField, eepromData.Cbus4);

	devEepromField = env->GetFieldID(devEepromClass, "RIsD2XX", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.RIsD2XX == 0 ? false:true));

// FT2232H Extensions - Version 3

	devEepromField = env->GetFieldID(devEepromClass, "pullDownEnable7", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.PullDownEnable7 == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "serNumEnable7", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.SerNumEnable7 == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "ALSlowSlew", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.ALSlowSlew == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "ALSchmittInput", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.ALSchmittInput == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "ALDriveCurrent", "B");
	env->SetByteField(devEeprom, devEepromField, eepromData.ALDriveCurrent);

	devEepromField = env->GetFieldID(devEepromClass, "AHSlowSlew", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.AHSlowSlew == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "AHSchmittInput", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.AHSchmittInput == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "AHDriveCurrent", "B");
	env->SetByteField(devEeprom, devEepromField, eepromData.ALDriveCurrent);

	devEepromField = env->GetFieldID(devEepromClass, "BLSlowSlew", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.BLSlowSlew == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "BLSchmittInput", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.BLSchmittInput == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "BLDriveCurrent", "B");
	env->SetByteField(devEeprom, devEepromField, eepromData.BLDriveCurrent);

	devEepromField = env->GetFieldID(devEepromClass, "BHSlowSlew", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.BHSlowSlew == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "BHSchmittInput", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.BHSchmittInput == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "BHDriveCurrent", "B");
	env->SetByteField(devEeprom, devEepromField, eepromData.BHDriveCurrent);

	devEepromField = env->GetFieldID(devEepromClass, "ifAIsFifo7", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.IFAIsFifo7 == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "ifAIsFifoTar7", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.IFAIsFifoTar7 == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "ifAIsFastSer7", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.IFAIsFastSer7 == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "AIsVCP7", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.AIsVCP7 == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "ifBIsFifo7", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.IFBIsFifo7 == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "ifBIsFifoTar7", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.IFBIsFifoTar7 == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "ifBIsFastSer7", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.IFBIsFastSer7 == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "BIsVCP7", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.BIsVCP7 == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "powerSaveEnable", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.PowerSaveEnable == 0 ? false:true));

// FT4232H Extensions - Version 4

	devEepromField = env->GetFieldID(devEepromClass, "pullDownEnable8", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.PullDownEnable8 == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "serNumEnable8", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.SerNumEnable8 == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "ASlowSlew", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.ASlowSlew == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "ASchmittInput", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.ASchmittInput == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "ADriveCurrent", "B");
	env->SetByteField(devEeprom, devEepromField, eepromData.ADriveCurrent);

	devEepromField = env->GetFieldID(devEepromClass, "BSlowSlew", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.BSlowSlew == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "BSchmittInput", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.BSchmittInput == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "BDriveCurrent", "B");
	env->SetByteField(devEeprom, devEepromField, eepromData.BDriveCurrent);

	devEepromField = env->GetFieldID(devEepromClass, "CSlowSlew", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.CSlowSlew == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "CSchmittInput", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.CSchmittInput == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "CDriveCurrent", "B");
	env->SetByteField(devEeprom, devEepromField, eepromData.CDriveCurrent);

	devEepromField = env->GetFieldID(devEepromClass, "DSlowSlew", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.DSlowSlew == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "DSchmittInput", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.DSchmittInput == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "DDriveCurrent", "B");
	env->SetByteField(devEeprom, devEepromField, eepromData.DDriveCurrent);

	devEepromField = env->GetFieldID(devEepromClass, "ARIIsTXDEN", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.ARIIsTXDEN == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "BRIIsTXDEN", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.BRIIsTXDEN == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "CRIIsTXDEN", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.CRIIsTXDEN == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "DRIIsTXDEN", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.DRIIsTXDEN == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "AIsVCP8", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.AIsVCP8 == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "BIsVCP8", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.BIsVCP8 == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "CIsVCP8", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.CIsVCP8 == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "DIsVCP8", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.DIsVCP8 == 0 ? false:true));

// FT232H Extensions - Version 5

	devEepromField = env->GetFieldID(devEepromClass, "pullDownEnableH", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.PullDownEnableH == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "serNumEnableH", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.SerNumEnableH == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "ACSlowSlewH", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.ACSlowSlewH == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "ACSchmittInputH", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.ACSchmittInputH == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "ACDriveCurrentH", "B");
	env->SetByteField(devEeprom, devEepromField, eepromData.ACDriveCurrentH);

	devEepromField = env->GetFieldID(devEepromClass, "ADSlowSlewH", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.ADSlowSlewH == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "ADSchmittInputH", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.ADSchmittInputH == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "ADDriveCurrentH", "B");
	env->SetByteField(devEeprom, devEepromField, eepromData.ADDriveCurrentH);

	devEepromField = env->GetFieldID(devEepromClass, "cbus0H", "B");
	env->SetByteField(devEeprom, devEepromField, eepromData.Cbus0H);

	devEepromField = env->GetFieldID(devEepromClass, "cbus1H", "B");
	env->SetByteField(devEeprom, devEepromField, eepromData.Cbus1H);

	devEepromField = env->GetFieldID(devEepromClass, "cbus2H", "B");
	env->SetByteField(devEeprom, devEepromField, eepromData.Cbus2H);

	devEepromField = env->GetFieldID(devEepromClass, "cbus3H", "B");
	env->SetByteField(devEeprom, devEepromField, eepromData.Cbus3H);

	devEepromField = env->GetFieldID(devEepromClass, "cbus4H", "B");
	env->SetByteField(devEeprom, devEepromField, eepromData.Cbus4H);

	devEepromField = env->GetFieldID(devEepromClass, "cbus5H", "B");
	env->SetByteField(devEeprom, devEepromField, eepromData.Cbus5H);

	devEepromField = env->GetFieldID(devEepromClass, "cbus6H", "B");
	env->SetByteField(devEeprom, devEepromField, eepromData.Cbus6H);

	devEepromField = env->GetFieldID(devEepromClass, "cbus7H", "B");
	env->SetByteField(devEeprom, devEepromField, eepromData.Cbus7H);

	devEepromField = env->GetFieldID(devEepromClass, "cbus8H", "B");
	env->SetByteField(devEeprom, devEepromField, eepromData.Cbus8H);

	devEepromField = env->GetFieldID(devEepromClass, "cbus9H", "B");
	env->SetByteField(devEeprom, devEepromField, eepromData.Cbus9H);

	devEepromField = env->GetFieldID(devEepromClass, "isFifoH", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.IsFifoH == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "isFifoTarH", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.IsFifoTarH == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "isFastSerH", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.IsFastSerH == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "isFT1248H", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.IsFT1248H == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "FT1248CpolH", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.FT1248CpolH == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "FT1248LsbH", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.FT1248LsbH == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "FT1248FlowControlH", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.FT1248FlowControlH == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "isVCPH", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.IsVCPH == 0 ? false:true));

	devEepromField = env->GetFieldID(devEepromClass, "powerSaveEnableH", "Z");
	env->SetBooleanField(devEeprom, devEepromField, (eepromData.PowerSaveEnableH == 0 ? false:true));

	env->DeleteLocalRef(devEepromClass);

	return devEeprom;
}




JNIEXPORT void JNICALL Java_com_ftdi_D2xx_eepromWrite (JNIEnv * env, jobject obj, jobject devEeprom)
{
	FT_STATUS status;
	FT_HANDLE handle;

	FT_PROGRAM_DATA eepromData;

	const char * manufacturerString;
	const char * manufacturerIdString;
	const char * descriptionString;
	const char * serialNumberString;

	jclass devEepromClass;
	jfieldID devEepromField;
	jstring devEepromString;


	handle = (FT_HANDLE)getNativeHandle(env, obj);

	// OK, got information on this device; populate our Java class
	devEepromClass = env->GetObjectClass(devEeprom);
	if (devEepromClass == 0)
	{
		LOGI("Failed to find EEPROM class");
		return;
	}

	// initialisation of structure
	eepromData.Signature1 = 0x00000000;
	eepromData.Signature2 = 0xFFFFFFFF;
	eepromData.Version = 0x00000005;	// use FT232H extensions

	// These fields must have these values:
	eepromData.PnP = 0;
	eepromData.IsoIn = 0;
	eepromData.IsoOut = 0;
	eepromData.IsoInA = 0;
	eepromData.IsoInB = 0;
	eepromData.IsoOutA = 0;
	eepromData.IsoOutB = 0;
	eepromData.EndpointSize = (UCHAR)64;

	// Map all other fields from the class passed in...
// Common elements
	devEepromField = env->GetFieldID(devEepromClass, "vendorId", "S");
	eepromData.VendorId = (USHORT)env->GetShortField(devEeprom, devEepromField);

	devEepromField = env->GetFieldID(devEepromClass, "productId", "S");
	eepromData.ProductId = (USHORT)env->GetShortField(devEeprom, devEepromField);

	devEepromField = env->GetFieldID(devEepromClass, "manufacturer", "Ljava/lang/String;");
	devEepromString = (jstring)env->GetObjectField(devEeprom, devEepromField);
	if (devEepromString != NULL)
	{
		manufacturerString = env->GetStringUTFChars(devEepromString, 0);
	}
	else
	{
		manufacturerString = NULL;
	}
	eepromData.Manufacturer = (char *)manufacturerString;

	devEepromField = env->GetFieldID(devEepromClass, "manufacturerId", "Ljava/lang/String;");
	devEepromString = (jstring)env->GetObjectField(devEeprom, devEepromField);
	if (devEepromString != NULL)
	{
		manufacturerIdString = env->GetStringUTFChars(devEepromString, 0);
	}
	else
	{
		manufacturerIdString = NULL;
	}
	eepromData.ManufacturerId = (char *)manufacturerIdString;

	devEepromField = env->GetFieldID(devEepromClass, "description", "Ljava/lang/String;");
	devEepromString = (jstring)env->GetObjectField(devEeprom, devEepromField);
	descriptionString = env->GetStringUTFChars(devEepromString, 0);
	if (devEepromString != NULL)
	{
		descriptionString = env->GetStringUTFChars(devEepromString, 0);
	}
	else
	{
		descriptionString = NULL;
	}
	eepromData.Description = (char *)descriptionString;

	devEepromField = env->GetFieldID(devEepromClass, "serialNumber", "Ljava/lang/String;");
	devEepromString = (jstring)env->GetObjectField(devEeprom, devEepromField);
	serialNumberString = env->GetStringUTFChars(devEepromString, 0);
	if (devEepromString != NULL)
	{
		serialNumberString = env->GetStringUTFChars(devEepromString, 0);
	}
	else
	{
		serialNumberString = NULL;
	}
	eepromData.SerialNumber = (char *)serialNumberString;

	devEepromField = env->GetFieldID(devEepromClass, "maxPower", "S");
	eepromData.MaxPower = (USHORT)env->GetShortField(devEeprom, devEepromField);

	devEepromField = env->GetFieldID(devEepromClass, "selfPowered", "Z");
	eepromData.SelfPowered = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "remoteWakeup", "Z");
	eepromData.RemoteWakeup = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

// FT232B extensions

	devEepromField = env->GetFieldID(devEepromClass, "rev4", "Z");
	eepromData.Rev4 = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "pullDownEnable", "Z");
	eepromData.PullDownEnable = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "serNumEnable", "Z");
	eepromData.SerNumEnable = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "USBVersionEnable", "Z");
	eepromData.USBVersionEnable = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "USBVersion", "S");
	eepromData.USBVersion = (USHORT)env->GetShortField(devEeprom, devEepromField);

// FT2232 extensions - Version 1

	devEepromField = env->GetFieldID(devEepromClass, "rev5", "Z");
	eepromData.Rev5 = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "pullDownEnable5", "Z");
	eepromData.PullDownEnable5 = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "serNumEnable5", "Z");
	eepromData.SerNumEnable5 = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "USBVersionEnable5", "Z");
	eepromData.USBVersionEnable5 = (env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "USBVersion5", "S");
	eepromData.USBVersion5 = (USHORT)env->GetShortField(devEeprom, devEepromField);

	devEepromField = env->GetFieldID(devEepromClass, "AIsHighCurrent", "Z");
	eepromData.AIsHighCurrent = (env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "BIsHighCurrent", "Z");
	eepromData.BIsHighCurrent = (env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "ifAIsFifo", "Z");
	eepromData.IFAIsFifo = (env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "ifAIsFifoTar", "Z");
	eepromData.IFAIsFifoTar = (env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "ifAIsFastSer", "Z");
	eepromData.IFAIsFastSer = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "AIsVCP", "Z");
	eepromData.AIsVCP = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "ifBIsFifo", "Z");
	eepromData.IFBIsFifo = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "ifBIsFifoTar", "Z");
	eepromData.IFBIsFifoTar = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "ifBIsFastSer", "Z");
	eepromData.IFBIsFastSer = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "BIsVCP", "Z");
	eepromData.BIsVCP = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

// FT232R extensions - Version 2

	devEepromField = env->GetFieldID(devEepromClass, "useExtOsc", "Z");
	eepromData.UseExtOsc = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "highDriveIOs", "Z");
	eepromData.HighDriveIOs = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "pullDownEnableR", "Z");
	eepromData.PullDownEnableR = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "serNumEnableR", "Z");
	eepromData.SerNumEnableR = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "invertTXD", "Z");
	eepromData.InvertTXD = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "invertRXD", "Z");
	eepromData.InvertRXD = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "invertRTS", "Z");
	eepromData.InvertRTS = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "invertCTS", "Z");
	eepromData.InvertCTS = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "invertDTR", "Z");
	eepromData.InvertDTR = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "invertDSR", "Z");
	eepromData.InvertDSR = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "invertDCD", "Z");
	eepromData.InvertDCD = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "invertRI", "Z");
	eepromData.InvertRI = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "cbus0", "B");
	eepromData.Cbus0 = (UCHAR)env->GetByteField(devEeprom, devEepromField);

	devEepromField = env->GetFieldID(devEepromClass, "cbus1", "B");
	eepromData.Cbus1 = (UCHAR)(UCHAR)env->GetByteField(devEeprom, devEepromField);

	devEepromField = env->GetFieldID(devEepromClass, "cbus2", "B");
	eepromData.Cbus2 = env->GetByteField(devEeprom, devEepromField);

	devEepromField = env->GetFieldID(devEepromClass, "cbus3", "B");
	eepromData.Cbus3 = (UCHAR)env->GetByteField(devEeprom, devEepromField);

	devEepromField = env->GetFieldID(devEepromClass, "cbus4", "B");
	eepromData.Cbus4 = (UCHAR)env->GetByteField(devEeprom, devEepromField);

	devEepromField = env->GetFieldID(devEepromClass, "RIsD2XX", "Z");
	eepromData.RIsD2XX = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

// FT2232H Extensions - Version 3

	devEepromField = env->GetFieldID(devEepromClass, "pullDownEnable7", "Z");
	eepromData.PullDownEnable7 = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "serNumEnable7", "Z");
	eepromData.SerNumEnable7 = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "ALSlowSlew", "Z");
	eepromData.ALSlowSlew = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "ALSchmittInput", "Z");
	eepromData.ALSchmittInput = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "ALDriveCurrent", "B");
	eepromData.ALDriveCurrent = (UCHAR)env->GetByteField(devEeprom, devEepromField);

	devEepromField = env->GetFieldID(devEepromClass, "AHSlowSlew", "Z");
	eepromData.AHSlowSlew = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "AHSchmittInput", "Z");
	eepromData.AHSchmittInput = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "AHDriveCurrent", "B");
	eepromData.ALDriveCurrent = (UCHAR)env->GetByteField(devEeprom, devEepromField);

	devEepromField = env->GetFieldID(devEepromClass, "BLSlowSlew", "Z");
	eepromData.BLSlowSlew = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "BLSchmittInput", "Z");
	eepromData.BLSchmittInput = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "BLDriveCurrent", "B");
	eepromData.BLDriveCurrent = (UCHAR)env->GetByteField(devEeprom, devEepromField);

	devEepromField = env->GetFieldID(devEepromClass, "BHSlowSlew", "Z");
	eepromData.BHSlowSlew = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "BHSchmittInput", "Z");
	eepromData.BHSchmittInput = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "BHDriveCurrent", "B");
	eepromData.BHDriveCurrent = (UCHAR)env->GetByteField(devEeprom, devEepromField);

	devEepromField = env->GetFieldID(devEepromClass, "ifAIsFifo7", "Z");
	eepromData.IFAIsFifo7 = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "ifAIsFifoTar7", "Z");
	eepromData.IFAIsFifoTar7 = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "ifAIsFastSer7", "Z");
	eepromData.IFAIsFastSer7 = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "AIsVCP7", "Z");
	eepromData.AIsVCP7 = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "ifBIsFifo7", "Z");
	eepromData.IFBIsFifo7 = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "ifBIsFifoTar7", "Z");
	eepromData.IFBIsFifoTar7 = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "ifBIsFastSer7", "Z");
	eepromData.IFBIsFastSer7 = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "BIsVCP7", "Z");
	eepromData.BIsVCP7 = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "powerSaveEnable", "Z");
	eepromData.PowerSaveEnable = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

// FT4232H Extensions - Version 4

	devEepromField = env->GetFieldID(devEepromClass, "pullDownEnable8", "Z");
	eepromData.PullDownEnable8 = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "serNumEnable8", "Z");
	eepromData.SerNumEnable8 = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "ASlowSlew", "Z");
	eepromData.ASlowSlew = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "ASchmittInput", "Z");
	eepromData.ASchmittInput = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "ADriveCurrent", "B");
	eepromData.ADriveCurrent = (UCHAR)env->GetByteField(devEeprom, devEepromField);

	devEepromField = env->GetFieldID(devEepromClass, "BSlowSlew", "Z");
	eepromData.BSlowSlew = (env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "BSchmittInput", "Z");
	eepromData.BSchmittInput = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "BDriveCurrent", "B");
	eepromData.BDriveCurrent = (UCHAR)env->GetByteField(devEeprom, devEepromField);

	devEepromField = env->GetFieldID(devEepromClass, "CSlowSlew", "Z");
	eepromData.CSlowSlew = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "CSchmittInput", "Z");
	eepromData.CSchmittInput = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "CDriveCurrent", "B");
	eepromData.CDriveCurrent = (UCHAR)(UCHAR)env->GetByteField(devEeprom, devEepromField);

	devEepromField = env->GetFieldID(devEepromClass, "DSlowSlew", "Z");
	eepromData.DSlowSlew = (env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "DSchmittInput", "Z");
	eepromData.DSchmittInput = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "DDriveCurrent", "B");
	eepromData.DDriveCurrent = (UCHAR)env->GetByteField(devEeprom, devEepromField);

	devEepromField = env->GetFieldID(devEepromClass, "ARIIsTXDEN", "Z");
	eepromData.ARIIsTXDEN = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "BRIIsTXDEN", "Z");
	eepromData.BRIIsTXDEN = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "CRIIsTXDEN", "Z");
	eepromData.CRIIsTXDEN = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "DRIIsTXDEN", "Z");
	eepromData.DRIIsTXDEN = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "AIsVCP8", "Z");
	eepromData.AIsVCP8 = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "BIsVCP8", "Z");
	eepromData.BIsVCP8 = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "CIsVCP8", "Z");
	eepromData.CIsVCP8 = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "DIsVCP8", "Z");
	eepromData.DIsVCP8 = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

// FT232H Extensions - Version 5

	devEepromField = env->GetFieldID(devEepromClass, "pullDownEnableH", "Z");
	eepromData.PullDownEnableH = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "serNumEnableH", "Z");
	eepromData.SerNumEnableH = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "ACSlowSlewH", "Z");
	eepromData.ACSlowSlewH = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "ACSchmittInputH", "Z");
	eepromData.ACSchmittInputH = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "ACDriveCurrentH", "B");
	eepromData.ACDriveCurrentH = (UCHAR)env->GetByteField(devEeprom, devEepromField);

	devEepromField = env->GetFieldID(devEepromClass, "ADSlowSlewH", "Z");
	eepromData.ADSlowSlewH = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "ADSchmittInputH", "Z");
	eepromData.ADSchmittInputH = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "ADDriveCurrentH", "B");
	eepromData.ADDriveCurrentH = (UCHAR)env->GetByteField(devEeprom, devEepromField);

	devEepromField = env->GetFieldID(devEepromClass, "cbus0H", "B");
	eepromData.Cbus0H = (UCHAR)env->GetByteField(devEeprom, devEepromField);

	devEepromField = env->GetFieldID(devEepromClass, "cbus1H", "B");
	eepromData.Cbus1H = (UCHAR)env->GetByteField(devEeprom, devEepromField);

	devEepromField = env->GetFieldID(devEepromClass, "cbus2H", "B");
	eepromData.Cbus2H = (UCHAR)env->GetByteField(devEeprom, devEepromField);

	devEepromField = env->GetFieldID(devEepromClass, "cbus3H", "B");
	eepromData.Cbus3H = (UCHAR)env->GetByteField(devEeprom, devEepromField);

	devEepromField = env->GetFieldID(devEepromClass, "cbus4H", "B");
	eepromData.Cbus4H = (UCHAR)env->GetByteField(devEeprom, devEepromField);

	devEepromField = env->GetFieldID(devEepromClass, "cbus5H", "B");
	eepromData.Cbus5H = (UCHAR)env->GetByteField(devEeprom, devEepromField);

	devEepromField = env->GetFieldID(devEepromClass, "cbus6H", "B");
	eepromData.Cbus6H = (UCHAR)env->GetByteField(devEeprom, devEepromField);

	devEepromField = env->GetFieldID(devEepromClass, "cbus7H", "B");
	eepromData.Cbus7H = (UCHAR)env->GetByteField(devEeprom, devEepromField);

	devEepromField = env->GetFieldID(devEepromClass, "cbus8H", "B");
	eepromData.Cbus8H = (UCHAR)env->GetByteField(devEeprom, devEepromField);

	devEepromField = env->GetFieldID(devEepromClass, "cbus9H", "B");
	eepromData.Cbus9H = (UCHAR)env->GetByteField(devEeprom, devEepromField);

	devEepromField = env->GetFieldID(devEepromClass, "isFifoH", "Z");
	eepromData.IsFifoH = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "isFifoTarH", "Z");
	eepromData.IsFifoTarH = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "isFastSerH", "Z");
	eepromData.IsFastSerH = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "isFT1248H", "Z");
	eepromData.IsFT1248H = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "FT1248CpolH", "Z");
	eepromData.FT1248CpolH = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "FT1248LsbH", "Z");
	eepromData.FT1248LsbH = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "FT1248FlowControlH", "Z");
	eepromData.FT1248FlowControlH = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "isVCPH", "Z");
	eepromData.IsVCPH = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);

	devEepromField = env->GetFieldID(devEepromClass, "powerSaveEnableH", "Z");
	eepromData.PowerSaveEnableH = (UCHAR)(env->GetBooleanField(devEeprom, devEepromField) == true ? 1:0);


	LOGI("> FT_EE_Program");
	status = FT_EE_Program(handle, &eepromData);
	LOGI("< FT_EE_Program (status = %d)", status);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_EE_Program");
	}

	env->ReleaseStringUTFChars(devEepromString, manufacturerString);
	env->ReleaseStringUTFChars(devEepromString, manufacturerIdString);
	env->ReleaseStringUTFChars(devEepromString, descriptionString);
	env->ReleaseStringUTFChars(devEepromString, serialNumberString);

	return;
}




JNIEXPORT jint JNICALL Java_com_ftdi_D2xx_eepromGetUserAreaSize (JNIEnv * env, jobject obj)
{
	FT_STATUS status;
	FT_HANDLE handle;
	DWORD uasize = 0;

	handle = (FT_HANDLE)getNativeHandle(env, obj);

	LOGI("> FT_EE_UASize");
	status = FT_EE_UASize(handle,&uasize);
	LOGI("< FT_EE_UASize (user area size = %d bytes), (status = %d)", uasize, status);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_EE_UASize");
	}

	return (jint)uasize;
}




JNIEXPORT jbyteArray JNICALL Java_com_ftdi_D2xx_eepromReadUserArea (JNIEnv * env, jobject obj)
{
	FT_STATUS status;
	FT_HANDLE handle;
	DWORD uasize;
	UCHAR * uadata;
	DWORD len = 0;

	jbyteArray data;

	handle = (FT_HANDLE)getNativeHandle(env,obj);

	// first, get the size of our user area
	status = FT_EE_UASize(handle,&uasize);
	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_EE_UASize");
		return data;
	}

	// allocate storage
	uadata = (UCHAR*)malloc(uasize);
	if (uadata == NULL)
	{
		// failed - no memory
		// return all elements as 0
		memset(data,0,uasize);
		// throw an exception for insufficient resources
		throwD2xxException(env, FT_INSUFFICIENT_RESOURCES, "FT_EE_UARead");
		return data;
	}

	data = env->NewByteArray(uasize);

	LOGI("> FT_EE_UARead (uasize = %d)",uasize);
	status = FT_EE_UARead(handle, uadata, uasize, &len);
	LOGI("< FT_EE_UARead (len = %d), (status = %d)", len, status);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_EE_UARead");
		free(uadata);
		return data;
	}

	// Now copy the user area data in to our Java array
	env->SetByteArrayRegion(data, 0, uasize, (jbyte*)uadata);

	free(uadata);

	return data;
}




JNIEXPORT void JNICALL Java_com_ftdi_D2xx_eepromWriteUserArea (JNIEnv * env, jobject obj, jbyteArray uadata)
{

	FT_STATUS status;
	FT_HANDLE handle;
	DWORD uasize;

	jbyte * buffer;
	DWORD len = 0;

	handle = (FT_HANDLE)getNativeHandle(env, obj);

	// first, get the size of our user area
	status = FT_EE_UASize(handle,&uasize);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_EE_UASize");
		return;
	}

	// check the size of the buffer to write
	if (uasize < env->GetArrayLength(uadata))
	{
		// too much data for the available space!
		// crop our value to the maximum permitted...
		LOGI("Attempt to write mode data than EEPROM UA capacity!");
		len = uasize;
	}
	else
	{
		// otherwise, write all data requested
		len = env->GetArrayLength(uadata);
	}

	buffer = env->GetByteArrayElements(uadata, 0);

	LOGI("> FT_EE_UAWrite (%d bytes)",len);
	status = FT_EE_UAWrite(handle, (UCHAR*)buffer, len);
	LOGI("< FT_EE_UAWrite (len = %d),(status = %d)", len, status);

	if (status != FT_OK)
	{
		// non-success status; throw an exception
		throwD2xxException(env, status, "FT_EE_UAWrite");
	}

	env->ReleaseByteArrayElements(uadata, buffer, 0);

	return;
}




/**********************************************************************************
 *
 * JNI function registration
 *
 **********************************************************************************/

static const char *classPathName = "com/ftdi/D2xx";

static JNINativeMethod sMethods[] = {
	/*Name							Signature									Function Pointer*/
/* STATIC METHODS */
	{"setVIDPID", 					"(II)V",									(void*)Java_com_ftdi_D2xx_setVIDPID},
	{"getVIDPID",					"()[I",										(void*)Java_com_ftdi_D2xx_getVIDPID},
	{"createDeviceInfoList",		"()I",										(void*)Java_com_ftdi_D2xx_createDeviceInfoList},
	{"getDeviceInfoList",			"(I[Lcom/ftdi/D2xx$FtDeviceInfoListNode;)I",(void*)Java_com_ftdi_D2xx_getDeviceInfoList},
	{"getDeviceInfoListDetail",		"(I)Lcom/ftdi/D2xx$FtDeviceInfoListNode;",	(void*)Java_com_ftdi_D2xx_getDeviceInfoListDetail},
	{"getLibraryVersion",			"()I",										(void*)Java_com_ftdi_D2xx_getLibraryVersion},
/* METHODS */
	{"openByIndex",					"(I)V",										(void*)Java_com_ftdi_D2xx_openByIndex},
	{"openBySerialNumber",			"(Ljava/lang/String;)V",					(void*)Java_com_ftdi_D2xx_openBySerialNumber},
	{"openByDescription",			"(Ljava/lang/String;)V",					(void*)Java_com_ftdi_D2xx_openByDescription},
	{"openByLocation",				"(I)V",										(void*)Java_com_ftdi_D2xx_openByLocation},
	{"close",						"()V",										(void*)Java_com_ftdi_D2xx_close},
	{"read",						"([BI)I",									(void*)Java_com_ftdi_D2xx_read},
	{"write",						"([BI)I",									(void*)Java_com_ftdi_D2xx_write},
	{"setBaudRate",					"(I)V",										(void*)Java_com_ftdi_D2xx_setBaudRate},
	{"setDataCharacteristics",		"(BBB)V",									(void*)Java_com_ftdi_D2xx_setDataCharacteristics},
	{"setFlowControl",				"(SBB)V",									(void*)Java_com_ftdi_D2xx_setFlowControl},
	{"setTimeouts",					"(II)V",									(void*)Java_com_ftdi_D2xx_setTimeouts},
	{"setDtr",						"()V",										(void*)Java_com_ftdi_D2xx_setDtr},
	{"clrDtr",						"()V",										(void*)Java_com_ftdi_D2xx_clrDtr},
	{"setRts",						"()V",										(void*)Java_com_ftdi_D2xx_setRts},
	{"clrRts",						"()V",										(void*)Java_com_ftdi_D2xx_clrRts},
	{"setBreakOn",					"()V",										(void*)Java_com_ftdi_D2xx_setBreakOn},
	{"setBreakOff",					"()V",										(void*)Java_com_ftdi_D2xx_setBreakOff},
	{"getModemStatus",				"()[B",										(void*)Java_com_ftdi_D2xx_getModemStatus},
	{"getQueueStatus",				"()I",										(void*)Java_com_ftdi_D2xx_getQueueStatus},
	{"getStatus",					"()[I",										(void*)Java_com_ftdi_D2xx_getStatus},
	{"purge",						"(B)V",										(void*)Java_com_ftdi_D2xx_purge},
	{"resetDevice",					"()V",										(void*)Java_com_ftdi_D2xx_resetDevice},
	{"stopInTask",					"()V",										(void*)Java_com_ftdi_D2xx_stopInTask},
	{"restartInTask",				"()V",										(void*)Java_com_ftdi_D2xx_restartInTask},
	{"getDeviceInfo",				"()Lcom/ftdi/D2xx$FtDeviceInfoListNode;",	(void*)Java_com_ftdi_D2xx_getDeviceInfo},
	{"setChars",					"(BBBB)V",									(void*)Java_com_ftdi_D2xx_setChars},
	{"setUSBParameters",			"(II)V",									(void*)Java_com_ftdi_D2xx_setUSBParameters},
	{"getDriverVersion",			"()I",										(void*)Java_com_ftdi_D2xx_getDriverVersion},
	{"setEventNotification", 		"(I)V",										(void*)Java_com_ftdi_D2xx_setEventNotification},
	{"waitEvent", 					"()V",										(void*)Java_com_ftdi_D2xx_waitEvent},
	{"waitEventTimed", 				"(I)Z",										(void*)Java_com_ftdi_D2xx_waitEventTimed},
/* EXTENDED METHODS FOR FT232B AND LATER */
	{"setLatencyTimer",				"(B)V",										(void*)Java_com_ftdi_D2xx_setLatencyTimer},
	{"getLatencyTimer",				"()B",										(void*)Java_com_ftdi_D2xx_getLatencyTimer},
	{"setBitMode",					"(BB)V",									(void*)Java_com_ftdi_D2xx_setBitMode},
	{"getPinStates",				"()B",										(void*)Java_com_ftdi_D2xx_getPinStates},
/* EEPROM METHODS */
	{"eepromReadWord",				"(I)S",										(void*)Java_com_ftdi_D2xx_eepromReadWord},
	{"eepromWriteWord",				"(IS)V",									(void*)Java_com_ftdi_D2xx_eepromWriteWord},
	{"eepromErase",					"()V",										(void*)Java_com_ftdi_D2xx_eepromErase},
	{"eepromRead",					"()Lcom/ftdi/D2xx$FtProgramData;",			(void*)Java_com_ftdi_D2xx_eepromRead},
	{"eepromWrite",					"(Lcom/ftdi/D2xx$FtProgramData;)V",			(void*)Java_com_ftdi_D2xx_eepromWrite},
	{"eepromGetUserAreaSize",		"()I",										(void*)Java_com_ftdi_D2xx_eepromGetUserAreaSize},
	{"eepromReadUserArea",			"()[B",										(void*)Java_com_ftdi_D2xx_eepromReadUserArea},
	{"eepromWriteUserArea",			"([B)V",									(void*)Java_com_ftdi_D2xx_eepromWriteUserArea}
};




int jniRegisterNativeMethods(JNIEnv * env, const char * className,
    const JNINativeMethod* gMethods, int numMethods)
{
    jclass clazz;

    LOGV("Registering %s natives\n", className);
    clazz = env->FindClass(className);
    if (clazz == NULL) {
        LOGE("Native registration unable to find class '%s'\n", className);
        return -1;
    }
    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        LOGE("RegisterNatives failed for '%s'\n", className);
        return -1;
    }
    return 0;
}


JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM * vm, void * reserved)
{
	JNIEnv *env;
	jclass clazz;

	LOGI("JNI_OnLoad called");
	if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
		LOGE("Failed to get the environment using GetEnv()");
		return -1;
	}

	jniRegisterNativeMethods(env, classPathName, sMethods, (sizeof(sMethods)/sizeof(sMethods[0])));

	// OK, now set up any other stuff we need
	// store our handle property identifier in a global...
	clazz = env->FindClass(classPathName);
	ftHandleId = env->GetFieldID(clazz, "ftHandle", "I");
	if (ftHandleId == 0)
	{
		LOGE("Failed to find ftHandleId in class!");
		return -1;
	}

	ftEventHandleId = env->GetFieldID(clazz, "ftEventHandle", "I");
	if (ftEventHandleId == 0)
	{
		LOGE("Failed to find ftEventHandleId in class!");
		return -1;
	}

	return JNI_VERSION_1_4;
}


