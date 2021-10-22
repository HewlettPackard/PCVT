//**********************************************************************
// (C) Copyright 2020-2021 Hewlett Packard Enterprise Development LP
//
// Permission is hereby granted, free of charge, to any person obtaining a
// copy of this software and associated documentation files (the "Software"),
// to deal in the Software without restriction, including without limitation
// the rights to use, copy, modify, merge, publish, distribute, sublicense,
// and/or sell copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included
// in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL
// THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
// OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
// ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
// OTHER DEALINGS IN THE SOFTWARE.
//**********************************************************************

package main

// #cgo CFLAGS: -Ibuild/zulu11jdk/include
// #cgo CFLAGS: -Ibuild/zulu11jdk/include/linux
// #include <jni.h>
// #include <stdlib.h>
// static jstring JavaStringFromCString(JNIEnv *env, const char *utf8) {
//   return (*env)->NewStringUTF(env, utf8);
// }
import "C"
import (
	"encoding/json"
	"fmt"
	"unsafe"
)

//export Java_devicesScan_NativeDiskScan_disksFetch
func Java_devicesScan_NativeDiskScan_disksFetch(env *C.JNIEnv, _ C.jclass) C.jstring {
	result, err := GetDisksInfo()
	if err != nil {
		panic(err)
	}

	data, err := json.Marshal(result)
	if err != nil {
		panic(err)
	}

	return JavaString(env, string(data))
}

//export Java_devicesScan_NativePlatformScan_platformFetch
func Java_devicesScan_NativePlatformScan_platformFetch(env *C.JNIEnv, _ C.jclass) C.jstring {
	result, err := getPlatformInfo()
	if err != nil {
		panic(err)
	}

	data, err := json.Marshal(result)
	if err != nil {
		panic(err)
	}

	return JavaString(env, string(data))
}

//export Java_devicesScan_NativeSmbiosScan_smbiosFetch
func Java_devicesScan_NativeSmbiosScan_smbiosFetch(env *C.JNIEnv, _ C.jclass) C.jstring {
	result, err := getSmbiosInfo()
	if err != nil {
		panic(err)
	}

	data, err := json.Marshal(result)
	if err != nil {
		panic(err)
	}

	return JavaString(env, string(data))
}

//export Java_devicesScan_NativePciCardsScan_pciCardsFetch
func Java_devicesScan_NativePciCardsScan_pciCardsFetch(env *C.JNIEnv, _ C.jclass) C.jstring {
	// result, err := GetDisksInfo()
	// if err != nil {
	// 	panic(err)
	// }

	slots, err := getSlotsV2()
	if err != nil {
		fmt.Println(err)
		panic(err)
	}

	cards, err := getPciCards(slots)
	if err != nil {
		fmt.Println(err)
		panic(err)
	}

	data, err := json.Marshal(cards)
	if err != nil {
		panic(err)
	}

	return JavaString(env, string(data))
}

func JavaString(env *C.JNIEnv, str string) C.jstring {
	cstr := C.CString(str)
	defer C.free(unsafe.Pointer(cstr))
	return C.JavaStringFromCString(env, cstr)
}

// func main() {}
