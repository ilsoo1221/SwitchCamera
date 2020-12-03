////OpenCL의 연산을 수행하는 곳 Gray하고 Blur할 부분을 OpenCL로 처리하기 위해 만들었다
////20201203오후2시43분
////작동안될시 전부 주석처리
//
//#define CL_FILE "data/local/tmp/Blur.cl"
//#define CL_FILEE "data/local/tmp/Gray.cl"
//#include <jni.h>
//#include <stdio.h>
//#include <stdlib.h>
//#include <string.h>
//#include <sys/time.h>
//#include <android/log.h>
//#include <android/bitmap.h>
//#include <CL/opencl.h>
//#include "jni.h"
//
//#define LOG_TAG "DEBUG"
////#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS)
//#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
//#define checkCL(expression){    \
//    cl_int err = (expression);  \
//    if(err<0 && err>-64){   \
//        printf("Error on line %d, error code: %d\n", __LINE__, err);    \
//        exit(0);    \
//    }   \
//}   \
//
//JNIEXPORT jobject JNICALL
//Java_com_example_switchcamera_SCActivity_SC_1ButtonFunction_BlurFilterClickWithOpenCL(JNIEnv *env, jobject thiz, jobject bitmap) {
//    // TODO: implement GrayFilterClickWithOpenCL()
//#define checkCL(expression){    \
//    cl_int err = (expression);  \
//    if(err<0 && err>-64){   \
//        LOGE("Error on line %d, error code: %d\n", __LINE__, err);    \
//        exit(0);    \
//    }   \
//}   \
// // TODO: implement GaussianBlurBitmap()
//    LOGE("reading bitmap info...");
//    AndroidBitmapInfo info;
//    int ret;
//    if((ret = AndroidBitmap_getInfo(env,bitmap,&info)) < 0){
//        LOGE("AndroidBitmap_getInfo() failed ! error=%d",ret);
//        return NULL;
//    }
//    LOGE("width:%d height:%d stride:%d",info.width, info.height, info.stride);
//    if(info.format != ANDROID_BITMAP_FORMAT_RGBA_8888){
//        LOGE("Bitmap format is not RGBA_8888!");
//        return NULL;
//    }
//    LOGE("reading bitmap pixels...");
//    void* bitmapPixels;
//    if((ret = AndroidBitmap_lockPixels(env,bitmap, &bitmapPixels)) < 0){
//        LOGE("AndroidBitmap_lockPixels() failed ! error=%d",ret);
//        return NULL;
//    }
//    uint32_t *src = (uint32_t*)bitmapPixels;
//    uint32_t *tempPixels = (uint32_t*)malloc(info.height * info.width*4);
//    int pixelsCount = info.height * info.width;
//    memcpy(tempPixels,src,sizeof(uint32_t) * pixelsCount);
//
//
//    char *kernel_file_buffer, *file_log;
//    size_t kernel_file_size, log_size;
//    FILE *file_handle;
//    unsigned char* kernel_name = "kernel_blur";
//
//    //Device input buffers
//    cl_mem d_src;
//    //Device output buffer
//    cl_mem d_dst;
//
//    cl_platform_id cpPlatform;  // OpenCl platform
//    cl_device_id device_id;     // device ID
//    cl_context context;         // context
//    cl_command_queue queue;     // command queue
//    cl_program program;         // program
//    cl_kernel kernel;           // kernel
//
//    file_handle=fopen(CL_FILE, "r");
//    if(file_handle == NULL){
//        LOGE("Couldn't find the file\n");
//        exit(1);
//    }
//
//    //read kernel file
//    fseek(file_handle, 0, SEEK_END);
//    kernel_file_size = ftell(file_handle);
//    rewind(file_handle);
//    kernel_file_buffer = (char*)malloc(kernel_file_size + 1);
//    kernel_file_buffer[kernel_file_size]= '\0';
//    fread(kernel_file_buffer, sizeof(char), kernel_file_size, file_handle);
//    fclose(file_handle);
//    size_t globalSize, localSize, grid;
//    //Number of work items in each local work group
//    localSize = 64;
//
//
//    // NUmber of total work items - localSize must be devisor
//    grid = ((pixelsCount)%localSize)? (pixelsCount/localSize)+1 : pixelsCount/localSize;
//    globalSize = grid * localSize;
//
//    cl_int err;
//    //bind to platform
//    checkCL(clGetPlatformIDs(1, &cpPlatform, NULL));
//
//    //get id for the device
//    checkCL(clGetDeviceIDs(cpPlatform, CL_DEVICE_TYPE_GPU, 1, &device_id, NULL));
//
//    //create a context
//    context = clCreateContext(0, 1, &device_id, NULL, NULL, &err);
//    checkCL(err);
//
//    //create a command queue
//    queue = clCreateCommandQueue(context, device_id, 0, &err);
//    checkCL(err);
//
//    //Create the compute program from the source buffer
//    program = clCreateProgramWithSource(context, 1,
//                                        (const char **) & kernel_file_buffer, &kernel_file_size, &err);
//    checkCL(err);
//    LOGE("bbbb");
//    //Build the program executable
//    // checkCL(clBuildProgram(program, 0, NULL, NULL, NULL, NULL));
//    LOGE("aaaa");
//    if(clBuildProgram(program, 0, NULL, NULL, NULL, NULL) != CL_SUCCESS)
//    {
//        LOGE("Program Build failed\n");
//        size_t length;
//        char buffer[2048];
//        clGetProgramBuildInfo(program, device_id, CL_PROGRAM_BUILD_LOG, sizeof(buffer), buffer, &length);
//        LOGE("--- Build log ---\n %s\n",buffer);
//        exit(1);
//    }
//
//
//
//
//    //Create the compute kernel in the program we wish to run
//    kernel = clCreateKernel(program, kernel_name, &err);
//    checkCL(err);
//    //Create the input and output arrays in device memory for our calculation
//    d_src = clCreateBuffer(context, CL_MEM_READ_ONLY, info.height * info.width*4, NULL, &err);
//    checkCL(err);
//    d_dst = clCreateBuffer(context, CL_MEM_WRITE_ONLY, info.height * info.width*4, NULL, &err);
//    checkCL(err);
//
//    //Write our data set into the input array in device mem
//    checkCL(clEnqueueWriteBuffer(queue, d_src, CL_TRUE, 0,
//                                 info.height * info.width*4, tempPixels, 0, NULL, NULL));
//
//    //set the arguments to our compute kernel
//    checkCL(clSetKernelArg(kernel, 0, sizeof(cl_mem), &d_src));
//    checkCL(clSetKernelArg(kernel, 1, sizeof(cl_mem), &d_dst));
//    checkCL(clSetKernelArg(kernel, 2, sizeof(int), &info.width));
//    checkCL(clSetKernelArg(kernel, 3, sizeof(int), &info.height));
//
//
//
//    //Execute the kernel over the entire range of the data set
//    checkCL(clEnqueueNDRangeKernel(queue, kernel, 1, NULL, &globalSize,
//                                   &localSize, 0, NULL, NULL));
//
//    //wait for the command queue to get serviced before reading back results
//    checkCL(clFinish(queue));  //kernel is asynchronous. need blocking read?
//
//    // Read the results from the device
//    checkCL(clEnqueueReadBuffer(queue, d_dst, CL_TRUE, 0,
//                                info.height * info.width*4, src, 0, NULL, NULL));
//
//    checkCL(clReleaseMemObject(d_src));
//    checkCL(clReleaseMemObject(d_dst));
//    checkCL(clReleaseProgram(program));
//    checkCL(clReleaseKernel(kernel));
//    checkCL(clReleaseCommandQueue(queue));
//    checkCL(clReleaseContext(context));
//
//    AndroidBitmap_unlockPixels(env, bitmap);
//    free(tempPixels);
//    return bitmap;
//
//}
//JNIEXPORT jobject JNICALL
//Java_com_example_switchcamera_SCActivity_SC_1ButtonFunction_GrayFilterClickWithOpenCL(JNIEnv *env, jobject thiz, jobject bitmap) {
//    // TODO: implement GrayFilterClickWithOpenCL()
//#define checkCL(expression){    \
//    cl_int err = (expression);  \
//    if(err<0 && err>-64){   \
//        LOGE("Error on line %d, error code: %d\n", __LINE__, err);    \
//        exit(0);    \
//    }   \
//}   \
// // TODO: implement GaussianBlurBitmap()
//    LOGE("reading bitmap info...");
//    AndroidBitmapInfo info;
//    int ret;
//    if((ret = AndroidBitmap_getInfo(env,bitmap,&info)) < 0){
//        LOGE("AndroidBitmap_getInfo() failed ! error=%d",ret);
//        return NULL;
//    }
//    LOGE("width:%d height:%d stride:%d",info.width, info.height, info.stride);
//    if(info.format != ANDROID_BITMAP_FORMAT_RGBA_8888){
//        LOGE("Bitmap format is not RGBA_8888!");
//        return NULL;
//    }
//    LOGE("reading bitmap pixels...");
//    void* bitmapPixels;
//    if((ret = AndroidBitmap_lockPixels(env,bitmap, &bitmapPixels)) < 0){
//        LOGE("AndroidBitmap_lockPixels() failed ! error=%d",ret);
//        return NULL;
//    }
//    uint32_t *src = (uint32_t*)bitmapPixels;
//    uint32_t *tempPixels = (uint32_t*)malloc(info.height * info.width*4);
//    int pixelsCount = info.height * info.width;
//    memcpy(tempPixels,src,sizeof(uint32_t) * pixelsCount);
//
//
//    char *kernel_file_buffer, *file_log;
//    size_t kernel_file_size, log_size;
//    FILE *file_handle;
//    unsigned char* kernel_name = "kernel_gray";
//
//    //Device input buffers
//    cl_mem d_src;
//    //Device output buffer
//    cl_mem d_dst;
//
//    cl_platform_id cpPlatform;  // OpenCl platform
//    cl_device_id device_id;     // device ID
//    cl_context context;         // context
//    cl_command_queue queue;     // command queue
//    cl_program program;         // program
//    cl_kernel kernel;           // kernel
//
//    file_handle=fopen(CL_FILEE, "r");
//    if(file_handle == NULL){
//        LOGE("Couldn't find the file\n");
//        exit(1);
//    }
//
//    //read kernel file
//    fseek(file_handle, 0, SEEK_END);
//    kernel_file_size = ftell(file_handle);
//    rewind(file_handle);
//    kernel_file_buffer = (char*)malloc(kernel_file_size + 1);
//    kernel_file_buffer[kernel_file_size]= '\0';
//    fread(kernel_file_buffer, sizeof(char), kernel_file_size, file_handle);
//    fclose(file_handle);
//    size_t globalSize, localSize, grid;
//    //Number of work items in each local work group
//    localSize = 64;
//
//
//    // NUmber of total work items - localSize must be devisor
//    grid = ((pixelsCount)%localSize)? (pixelsCount/localSize)+1 : pixelsCount/localSize;
//    globalSize = grid * localSize;
//
//    cl_int err;
//    //bind to platform
//    checkCL(clGetPlatformIDs(1, &cpPlatform, NULL));
//
//    //get id for the device
//    checkCL(clGetDeviceIDs(cpPlatform, CL_DEVICE_TYPE_GPU, 1, &device_id, NULL));
//
//    //create a context
//    context = clCreateContext(0, 1, &device_id, NULL, NULL, &err);
//    checkCL(err);
//
//    //create a command queue
//    queue = clCreateCommandQueue(context, device_id, 0, &err);
//    checkCL(err);
//
//    //Create the compute program from the source buffer
//    program = clCreateProgramWithSource(context, 1,
//                                        (const char **) & kernel_file_buffer, &kernel_file_size, &err);
//    checkCL(err);
//    LOGE("bbbb");
//    //Build the program executable
//    // checkCL(clBuildProgram(program, 0, NULL, NULL, NULL, NULL));
//    LOGE("aaaa");
//    if(clBuildProgram(program, 0, NULL, NULL, NULL, NULL) != CL_SUCCESS)
//    {
//        LOGE("Program Build failed\n");
//        size_t length;
//        char buffer[2048];
//        clGetProgramBuildInfo(program, device_id, CL_PROGRAM_BUILD_LOG, sizeof(buffer), buffer, &length);
//        LOGE("--- Build log ---\n %s\n",buffer);
//        exit(1);
//    }
//
//
//
//
//    //Create the compute kernel in the program we wish to run
//    kernel = clCreateKernel(program, kernel_name, &err);
//    checkCL(err);
//    //Create the input and output arrays in device memory for our calculation
//    d_src = clCreateBuffer(context, CL_MEM_READ_ONLY, info.height * info.width*4, NULL, &err);
//    checkCL(err);
//    d_dst = clCreateBuffer(context, CL_MEM_WRITE_ONLY, info.height * info.width*4, NULL, &err);
//    checkCL(err);
//
//    //Write our data set into the input array in device mem
//    checkCL(clEnqueueWriteBuffer(queue, d_src, CL_TRUE, 0,
//                                 info.height * info.width*4, tempPixels, 0, NULL, NULL));
//
//    //set the arguments to our compute kernel
//    checkCL(clSetKernelArg(kernel, 0, sizeof(cl_mem), &d_src));
//    checkCL(clSetKernelArg(kernel, 1, sizeof(cl_mem), &d_dst));
//    checkCL(clSetKernelArg(kernel, 2, sizeof(int), &info.width));
//    checkCL(clSetKernelArg(kernel, 3, sizeof(int), &info.height));
//
//
//
//    //Execute the kernel over the entire range of the data set
//    checkCL(clEnqueueNDRangeKernel(queue, kernel, 1, NULL, &globalSize,
//                                   &localSize, 0, NULL, NULL));
//
//    //wait for the command queue to get serviced before reading back results
//    checkCL(clFinish(queue));  //kernel is asynchronous. need blocking read?
//
//    // Read the results from the device
//    checkCL(clEnqueueReadBuffer(queue, d_dst, CL_TRUE, 0,
//                                info.height * info.width*4, src, 0, NULL, NULL));
//
//    checkCL(clReleaseMemObject(d_src));
//    checkCL(clReleaseMemObject(d_dst));
//    checkCL(clReleaseProgram(program));
//    checkCL(clReleaseKernel(kernel));
//    checkCL(clReleaseCommandQueue(queue));
//    checkCL(clReleaseContext(context));
//
//    AndroidBitmap_unlockPixels(env, bitmap);
//    free(tempPixels);
//    return bitmap;
//
//}
//
//
//
