#include <util_init.hpp>
#include <cstdlib>
#include <vector>

static const float vertexData[] = {
        // L:left, T:top, R:right, B:bottom, C:center
        -1.0f, -1.0f, 0.0f, // LT
        -1.0f,  0.0f, 0.0f, // LC
        0.0f, -1.0f, 0.0f, // CT
        0.0f,  0.0f, 0.0f, // CC
        1.0f, -1.0f, 0.0f, // RT
        1.0f,  0.0f, 0.0f, // RC
        -1.0f,  0.0f, 0.0f, // LC
        -1.0f,  1.0f, 0.0f, // LB
        0.0f,  0.0f, 0.0f, // CC
        0.0f,  1.0f, 0.0f, // CB
        1.0f,  0.0f, 0.0f, // RC
        1.0f,  1.0f, 0.0f, // RB
};

static const float fragData[] = {
        1.0f, 0.0f, 0.0f, // Red
        0.0f, 1.0f, 0.0f, // Green
        0.0f, 0.0f, 1.0f, // Blue
        1.0f, 1.0f, 0.0f, // Yellow
};


static const char *vertShaderText =
        "#version 400\n"
        "#extension GL_ARB_separate_shader_objects : enable\n"
        "#extension GL_ARB_shading_language_420pack : enable\n"
        "        layout (location = 0) in vec4 pos;\n"
        "void main() {\n"
        "    gl_Position = pos;\n"
        "}\n";

static const char *fragShaderText =
        "#version 400\n"
        "#extension GL_ARB_separate_shader_objects : enable\n"
        "#extension GL_ARB_shading_language_420pack : enable\n"
        "layout (location = 0) out vec4 uFragColor;\n"
        "layout (push_constant) uniform ColorData {\n"
        "        vec3 colors;\n"
        "} colorData;\n"
        "void main() {\n"
        "    uFragColor = vec4(colorData.colors, 1.0);\n"
        "}\n";

bool enumerateInstanceExtensions(std::vector<VkExtensionProperties>* extensions) {
    VkResult result;
    uint32_t count = 0;

    result = vkEnumerateInstanceExtensionProperties(nullptr, &count, nullptr);
    if (result != VK_SUCCESS) return false;

    std::cout<<"[MTKPGPU-APP] Get " << count << " instance extensions" << std::endl;

    extensions->resize(count);
    result = vkEnumerateInstanceExtensionProperties(nullptr, &count, extensions->data());
    if (result != VK_SUCCESS) return false;

    return true;
}

bool enumerateDeviceExtensions(VkPhysicalDevice device, std::vector<VkExtensionProperties>* extensions) {
    VkResult result;

    uint32_t count = 0;
    result = vkEnumerateDeviceExtensionProperties(device, nullptr, &count, nullptr);
    if (result != VK_SUCCESS) return false;

    std::cout<<"[MTKPGPU-APP] Get " << count << " device extensions" << std::endl;

    extensions->resize(count);
    result = vkEnumerateDeviceExtensionProperties(device, nullptr, &count, extensions->data());
    if (result != VK_SUCCESS) return false;

    return true;
}

void dumpExtensions(bool isInst, std::vector<VkExtensionProperties> *extensions) {
    if (isInst) {
        std::cout << "=========[MTKPGPU-APP] [Instance Extensions] =========" << std::endl;
    } else {
        std::cout << "=========[MTKPGPU-APP] [Device Extensions] =========" << std::endl;
    }
    for (uint32_t i = 0; i < extensions->size(); i++) {
        VkExtensionProperties *props = &((*extensions)[i]);
        std::cout << "[MTKPGPU-APP] " << props->extensionName << ":" << std::endl;
        std::cout << "[MTKPGPU-APP] " << "\tVersion: " << props->specVersion << std::endl;
        std::cout << std::endl << std::endl;
    }
    std::cout << std::endl;
}

void setupDeviceInfo(struct sample_info &info) {
    uint32_t gpu_count = 1;
    vkEnumeratePhysicalDevices(info.inst, &gpu_count, NULL);
    info.gpus.resize(gpu_count);
    vkEnumeratePhysicalDevices(info.inst, &gpu_count, info.gpus.data());
}

void checkInstAndDevExtensions(struct sample_info &info) {

    std::vector<VkExtensionProperties> *vk_inst_ext = new std::vector<VkExtensionProperties>[1];
    std::vector<VkExtensionProperties> *vk_dev_ext = new std::vector<VkExtensionProperties>[1];

    if (! enumerateInstanceExtensions(vk_inst_ext) ) {
        std::cout << "[MTKPGPU-APP] something went wrong when we query inst extensions"<<std::endl;
    } else {
        dumpExtensions(true, vk_inst_ext);
    }

    setupDeviceInfo(info);

    if (! enumerateDeviceExtensions(info.gpus[0], vk_dev_ext)) {
        std::cout << "[MTKPGPU-APP] something went wrong when we query dev extensions"<<std::endl;
    } else {
        dumpExtensions(false, vk_dev_ext);
    }
}

void createAndroidSurface(struct sample_info &info) {

    init_enumerate_device(info);
    init_window_size(info, 500, 500);
    init_connection(info);
    init_window(info);

    const VkAndroidSurfaceCreateInfoKHR surfaceInfo = {
            .sType = VK_STRUCTURE_TYPE_ANDROID_SURFACE_CREATE_INFO_KHR,
            .pNext = nullptr,
            .flags = 0,
            .window = AndroidGetApplicationWindow(),
    };

    VkResult res = vkCreateAndroidSurfaceKHR(info.inst, &surfaceInfo, nullptr, &info.surface);

    if (res != VK_SUCCESS) {
        std::cout << "[MTKPGPU-APP] something went wrong while dequeue a surface: " << res << std::endl;
    } else {
        std::cout << "[MTKPGPU-APP] Get surface " << info.surface << std::endl;
    }
}

void checkSurfaceCapabilities(struct sample_info &info, VkSurfaceCapabilitiesKHR &surfCapabilities) {
    VkResult res;
    res = vkGetPhysicalDeviceSurfaceCapabilitiesKHR(info.gpus[0], info.surface, &surfCapabilities);
    if (VK_SUCCESS == res) {
        std::cout<<"[MTKPGPU-APP] ========= Vulkan Surface Capabilities =========="<<std::endl;
        std::cout<<"[MTKPGPU-APP] \t minImageCount: "<<surfCapabilities.minImageCount<<std::endl;
        std::cout<<"[MTKPGPU-APP] \t maxImageCount: "<<surfCapabilities.maxImageCount<<std::endl;
        std::cout<<"[MTKPGPU-APP] \t maxArrayImageCount: "<<surfCapabilities.maxImageArrayLayers<<std::endl;
        std::cout<<"[MTKPGPU-APP] \t current image width: "<<surfCapabilities.currentExtent.width<<std::endl;
        std::cout<<"[MTKPGPU-APP] \t current image height: "<<surfCapabilities.currentExtent.height<<std::endl;
        std::cout<<"[MTKPGPU-APP] \t min image scale width: "<<surfCapabilities.minImageExtent.width<<std::endl;
        std::cout<<"[MTKPGPU-APP] \t min image scale height: "<<surfCapabilities.minImageExtent.height<<std::endl;
        std::cout<<"[MTKPGPU-APP] \t max image scale width: "<<surfCapabilities.maxImageExtent.width<<std::endl;
        std::cout<<"[MTKPGPU-APP] \t max image scale height: "<<surfCapabilities.maxImageExtent.height<<std::endl;
        std::cout<<"[MTKPGPU-APP] \t allowed surface usage: "<<surfCapabilities.supportedUsageFlags<<std::endl;
        std::cout<<"[MTKPGPU-APP] \t current transform mode: "<<surfCapabilities.currentTransform<<std::endl;
        std::cout<<"[MTKPGPU-APP] \t support transform mode: "<<surfCapabilities.supportedTransforms<<std::endl;
        std::cout<<"[MTKPGPU-APP] \t support compose mode: "<<surfCapabilities.supportedCompositeAlpha<<std::endl;
    } else {
        std::cout<<"[MTKPGPU-APP] something went wrong while query surface capabilities"<<std::endl;
    }
}

bool checkQueueSupportPresent(struct sample_info &info) {
    // note: we have only one device -- gpus[0] on Boar and Pixel 2 XL
    VkBool32 *pSupportsPresent = (VkBool32 *)malloc(info.queue_family_count * sizeof(VkBool32));

    info.graphics_queue_family_index = UINT32_MAX;
    info.present_queue_family_index = UINT32_MAX;

    for (uint32_t i = 0; i < info.queue_family_count; i++) {
        vkGetPhysicalDeviceSurfaceSupportKHR(info.gpus[0], i, info.surface, &(pSupportsPresent[i]));

        std::cout<<"[MTKPGPU-APP] Queue-Family [ "<< i <<" ] support presentation? "<< pSupportsPresent[i]<<std::endl;
        if (VK_TRUE == pSupportsPresent[i] && (info.queue_props[i].queueFlags & VK_QUEUE_GRAPHICS_BIT)) {
            info.graphics_queue_family_index = i;
            info.present_queue_family_index = i;
            break;
        }
    }
    // if we cannot find a queue which supports both graphics and presentation,
    // return fail immediately
    if (UINT32_MAX == info.graphics_queue_family_index || UINT32_MAX == info.present_queue_family_index) {
        std::cout << "[MTKPGPU-APP] Cannot find a queue to support both graphics and presentation"<<std::endl;
        free(pSupportsPresent);
        return false;
    }
    free(pSupportsPresent);
    std::cout<<"[MTKPGPU-APP] targeting queue family ["<<info.graphics_queue_family_index<<"] to do the GFX jobs"<<std::endl;

    uint32_t formatCount;
    VkResult res = vkGetPhysicalDeviceSurfaceFormatsKHR(info.gpus[0], info.surface, &formatCount, NULL);
    if (VK_SUCCESS == res) {
        std::cout<<"[MTKPGPU-APP] surface supports "<<formatCount<< " formats"<<std::endl;
        std::vector<VkSurfaceFormatKHR> surfFormats(formatCount);
        vkGetPhysicalDeviceSurfaceFormatsKHR(info.gpus[0], info.surface, &formatCount, surfFormats.data());

        int32_t r8g8b8a8_unorm_idx = -1;
        for (uint32_t i = 0; i < surfFormats.size(); i++) {
            std::cout<<"[MTKPGPU-APP] surface supports format:"<< surfFormats.at(i).format<<std::endl;
            std::cout<<"[MTKPGPU-APP] surface supports colorspace:"<< surfFormats.at(i).colorSpace<<std::endl;
            if (VK_FORMAT_R8G8B8A8_UNORM == surfFormats.at(i).format) {
                std::cout<<"[MTKPGPU-APP] CTS only needs VK_FORMAT_R8G8B8A8_UNORM but I don't know why.."<<std::endl;
                r8g8b8a8_unorm_idx = i;
            }
        }

        if (r8g8b8a8_unorm_idx != -1) {
            info.format = surfFormats.at(r8g8b8a8_unorm_idx).format;
        } else {
            return false;
        }
    }
    return true;
}

bool createDevice(struct sample_info &info) {
    VkResult res;
    const float priority = 1.0f;

    const VkDeviceQueueCreateInfo queueCreateInfo = {
            .sType = VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO,
            .pNext = nullptr,
            .flags = 0,
            .queueFamilyIndex = info.graphics_queue_family_index,
            .queueCount = 1,
            .pQueuePriorities = &priority,
    };
    const VkDeviceCreateInfo deviceCreateInfo = {
            .sType = VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO,
            .pNext = nullptr,
            .queueCreateInfoCount = 1,
            .pQueueCreateInfos = &queueCreateInfo,
            .enabledLayerCount = 0,
            .ppEnabledLayerNames = nullptr,
            .enabledExtensionCount = static_cast<uint32_t>(info.device_extension_names.size()),
            .ppEnabledExtensionNames = info.device_extension_names.data(),
            .pEnabledFeatures = nullptr,
    };

    //vkCreateDevice(mGpu, &deviceCreateInfo, nullptr, &mDevice);
    //vkGetDeviceQueue(mDevice, mQueueFamilyIndex, 0, &mQueue);
    res = vkCreateDevice(info.gpus[0], &deviceCreateInfo, NULL, &info.device);
    if (VK_SUCCESS != res) {
        std::cout<<"[MTKPGPU-APP] something went wrong while creating device"<<std::endl;
        return false;
    } else {
        std::cout<<"[MTKPGPU-APP] device is created"<<std::endl;
        return true;
    }
}

bool obtainQueue(struct sample_info &info) {
    vkGetDeviceQueue(info.device, info.graphics_queue_family_index, 0, &info.graphics_queue);
    if (info.graphics_queue_family_index == info.present_queue_family_index) {
        info.present_queue = info.graphics_queue;
        std::cout<<"[MTKPGPU-APP] graphics queue is the same as present queue"<<std::endl;
    } else {
        std::cout<<"[MTKPGPU-APP] graphics queue is different from present queue"<<std::endl;
        vkGetDeviceQueue(info.device, info.present_queue_family_index, 0, &info.present_queue);
    }

    if (VK_NULL_HANDLE == info.graphics_queue || VK_NULL_HANDLE == info.present_queue) {
        std::cout<<"[MTKPGPU-APP] something went wrong while obtaining the queue"<<std::endl;
        return false;
    } else {
        std::cout<<"[MTKPGPU-APP] queue is obtained"<<std::endl;
        return true;
    }
}

bool createCommandPool(struct sample_info &info) {

    const VkCommandPoolCreateInfo commandPoolCreateInfo = {
            .sType = VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO,
            .pNext = nullptr,
            .flags = VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT,
            .queueFamilyIndex = info.graphics_queue_family_index,
    };

    if (VK_SUCCESS == vkCreateCommandPool(info.device, &commandPoolCreateInfo, NULL, &info.cmd_pool)){
        std::cout<<"[MTKPGPU-APP] command pool is created"<<std::endl;
        return true;
    } else {
        std::cout<<"[MTKPGPU-APP] something went wrong while creating command pool"<<std::endl;
        return false;
    }
}

bool createCommandBuffer(struct sample_info &info, std::vector<VkCommandBuffer> &commandBuffers) {
    VkCommandBufferAllocateInfo vkCommandBufferAllocateInfo = {
            .sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO,
            .pNext = nullptr,
            .commandPool = info.cmd_pool,
            .level = VK_COMMAND_BUFFER_LEVEL_PRIMARY,
            .commandBufferCount = info.swapchainImageCount,
    };
    commandBuffers.resize(info.swapchainImageCount);


    if (VK_SUCCESS == vkAllocateCommandBuffers(info.device, &vkCommandBufferAllocateInfo, &info.cmd)){
        std::cout<<"[MTKPGPU-APP] " << info.swapchainImageCount <<" command buffer is created"<<std::endl;
    } else {
        std::cout<<"[MTKPGPU-APP] something went wrong while creating command buffer"<<std::endl;
        return false;
    }

    if (VK_SUCCESS == vkAllocateCommandBuffers(info.device, &vkCommandBufferAllocateInfo, commandBuffers.data())){
        std::cout<<"[MTKPGPU-APP] workaround cmdbuffer: " << info.swapchainImageCount <<" command buffer is created"<<std::endl;

        for (uint32_t i = 0; i < info.swapchainImageCount;++i) {
            std::cout << "[MTKPGPU-APP] workaround command buffer " << i << ": "<< commandBuffers[i] << std::endl;
        }
    } else {
        std::cout<<"[MTKPGPU-APP] something went wrong while creating workaround command buffer"<<std::endl;
        return false;
    }

    return true;
}

bool createSwapchain(struct sample_info &info, VkSurfaceCapabilitiesKHR &surfCapabilities,
                     VkExtent2D &displaySize, bool rotate) {
    displaySize = surfCapabilities.currentExtent;
    VkSurfaceTransformFlagBitsKHR preTransform = (rotate ?
                                                  surfCapabilities.currentTransform :
                                                  VK_SURFACE_TRANSFORM_IDENTITY_BIT_KHR);

    std::cout<<"[MTKPGPU-APP] currentTransform is " << static_cast<uint32_t>(surfCapabilities.currentTransform) <<std::endl;
    std::cout<<"[MTKPGPU-APP] set rotate to "<< static_cast<uint32_t>(preTransform)<<std::endl;

    if ((preTransform &
         (VK_SURFACE_TRANSFORM_ROTATE_90_BIT_KHR | VK_SURFACE_TRANSFORM_ROTATE_270_BIT_KHR |
          VK_SURFACE_TRANSFORM_HORIZONTAL_MIRROR_ROTATE_90_BIT_KHR |
          VK_SURFACE_TRANSFORM_HORIZONTAL_MIRROR_ROTATE_270_BIT_KHR)) != 0) {
        // basically change landscape to portrait
        // however, what will happen on TV devices?
        std::cout<<"[MTKPGPU-APP] swap width & height due to existing rotation"<<std::endl;
        std::swap(displaySize.width, displaySize.height);
    }

    // Queue family with VK_QUEUE_GRAPHICS_BIT
    const uint32_t queueFamilyIndex = info.graphics_queue_family_index;

    const VkSwapchainCreateInfoKHR swapchainCreateInfo = {
            .sType = VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR,
            .pNext = nullptr,
            .flags = 0,
            .surface = info.surface,
            .minImageCount = surfCapabilities.minImageCount,
            .imageFormat = info.format,
            .imageColorSpace = VK_COLOR_SPACE_SRGB_NONLINEAR_KHR, // hardcoded!
            .imageExtent = displaySize,
            .imageUsage = VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT,
            .preTransform = preTransform,
            .imageArrayLayers = 1,
            .imageSharingMode = VK_SHARING_MODE_EXCLUSIVE,
            .queueFamilyIndexCount = 1,
            .pQueueFamilyIndices = &queueFamilyIndex,
            .compositeAlpha = VK_COMPOSITE_ALPHA_INHERIT_BIT_KHR, //hardcoded
            .presentMode = VK_PRESENT_MODE_FIFO_KHR,
            .clipped = VK_FALSE,
    };

    if (VK_SUCCESS == vkCreateSwapchainKHR(info.device, &swapchainCreateInfo, NULL, &info.swap_chain)) {
        std::cout<<"[MTKPGPU-APP] swapchain is created"<<std::endl;

        if (VK_SUCCESS == vkGetSwapchainImagesKHR(info.device, info.swap_chain, &info.swapchainImageCount, NULL)) {
            std::cout<<"[MTKPGPU-APP] swapchain length is "<<info.swapchainImageCount<<std::endl;
        } else {
            std::cout<<"[MTKPGPU-APP] something went wrong while obtaining swapchain"<<std::endl;
            return false;
        }
    } else {
        std::cout<<"[MTKPGPU-APP] something went wrong while creating swapchain"<<std::endl;
        return false;
    }

    return true;
}

bool createRenderPass(struct sample_info &info) {

    const VkAttachmentDescription attachmentDescription = {
            .flags = 0,
            .format = info.format,
            .samples = VK_SAMPLE_COUNT_1_BIT,
            .loadOp = VK_ATTACHMENT_LOAD_OP_CLEAR,
            .storeOp = VK_ATTACHMENT_STORE_OP_STORE,
            .stencilLoadOp = VK_ATTACHMENT_LOAD_OP_DONT_CARE,
            .stencilStoreOp = VK_ATTACHMENT_STORE_OP_DONT_CARE,
            .initialLayout = VK_IMAGE_LAYOUT_UNDEFINED,
            .finalLayout = VK_IMAGE_LAYOUT_PRESENT_SRC_KHR,
    };
    const VkAttachmentReference attachmentReference = {
            .attachment = 0,
            .layout = VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
    };
    const VkSubpassDescription subpassDescription = {
            .flags = 0,
            .pipelineBindPoint = VK_PIPELINE_BIND_POINT_GRAPHICS,
            .inputAttachmentCount = 0,
            .pInputAttachments = nullptr,
            .colorAttachmentCount = 1,
            .pColorAttachments = &attachmentReference,
            .pResolveAttachments = nullptr,
            .pDepthStencilAttachment = nullptr,
            .preserveAttachmentCount = 0,
            .pPreserveAttachments = nullptr,
    };
    const VkRenderPassCreateInfo renderPassCreateInfo = {
            .sType = VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO,
            .pNext = nullptr,
            .flags = 0,
            .attachmentCount = 1,
            .pAttachments = &attachmentDescription,
            .subpassCount = 1,
            .pSubpasses = &subpassDescription,
            .dependencyCount = 0,
            .pDependencies = nullptr,
    };

    if (VK_SUCCESS == vkCreateRenderPass(info.device, &renderPassCreateInfo, nullptr, &info.render_pass)){
        if (info.render_pass != VK_NULL_HANDLE) {
            std::cout<<"[MTKPGPU-APP] renderpass is created"<<std::endl;
            return true;
        } else {
            std::cout<<"[MTKPGPU-APP] something went wrong while creating renderpass -- null handle"<<std::endl;
            return false;
        }
    } else {
        std::cout<<"[MTKPGPU-APP] something went wrong while creating renderpass"<<std::endl;
        return false;
    }
}

bool createFramebuffers(struct sample_info &info, VkExtent2D &displaySize) {

    uint32_t swapchainLength = info.swapchainImageCount;
    info.framebuffers = (VkFramebuffer *)malloc(info.swapchainImageCount * sizeof(VkFramebuffer));
    VkImage *swapchainImages = (VkImage *)malloc(info.swapchainImageCount * sizeof(VkImage));
    vkGetSwapchainImagesKHR(info.device, info.swap_chain, &swapchainLength, swapchainImages);

    for (uint32_t i = 0; i < swapchainLength; ++i) {

        swap_chain_buffer sc_buffer;
        sc_buffer.image = swapchainImages[i];

        const VkImageViewCreateInfo imageViewCreateInfo = {
                .sType = VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO,
                .pNext = nullptr,
                .flags = 0,
                .image = sc_buffer.image,
                .viewType = VK_IMAGE_VIEW_TYPE_2D,
                .format = info.format,
                .components = {
                        .r = VK_COMPONENT_SWIZZLE_R,
                        .g = VK_COMPONENT_SWIZZLE_G,
                        .b = VK_COMPONENT_SWIZZLE_B,
                        .a = VK_COMPONENT_SWIZZLE_A,
                },
                .subresourceRange = {
                        .aspectMask = VK_IMAGE_ASPECT_COLOR_BIT,
                        .baseMipLevel = 0,
                        .levelCount = 1,
                        .baseArrayLayer = 0,
                        .layerCount = 1,
                },
        };
        if (VK_SUCCESS == vkCreateImageView(info.device, &imageViewCreateInfo, nullptr, &sc_buffer.view)) {
            std::cout<<"[MTKPGPU-APP] image view "<< i << " is created: "<< sc_buffer.view<<std::endl;
            info.buffers.push_back(sc_buffer);
        } else {
            std::cout<<"[MTKPGPU-APP] something went wrong while creating image view "<< i <<std::endl;
            return false;
        }
    }

    free(swapchainImages);
    info.current_buffer = 0;

    for (uint32_t i = 0; i < swapchainLength; ++i) {
        const VkFramebufferCreateInfo framebufferCreateInfo = {
                .sType = VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO,
                .pNext = nullptr,
                .flags = 0,
                .renderPass = info.render_pass,
                .attachmentCount = 1,
                .pAttachments = &info.buffers[i].view,
                .width = displaySize.width,
                .height = displaySize.height,
                .layers = 1,
        };
        if (VK_SUCCESS == vkCreateFramebuffer(info.device, &framebufferCreateInfo, nullptr, &info.framebuffers[i])) {
            std::cout<<"[MTKPGPU-APP] framebuffer "<< i << " is created: "<<info.framebuffers[i]<<std::endl;
        } else {
            std::cout<<"[MTKPGPU-APP] something went wrong while creating framebuffer "<< i <<std::endl;
            return false;
        }
    }

    return true;
}

bool createVertexBuffers(struct sample_info &info) {
    const VkBufferCreateInfo bufferCreateInfo = {
            .sType = VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO,
            .pNext = nullptr,
            .flags = 0,
            .size = sizeof(vertexData),
            .usage = VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
            .sharingMode = VK_SHARING_MODE_EXCLUSIVE,
            .queueFamilyIndexCount = 1,
            .pQueueFamilyIndices = &(info.graphics_queue_family_index),
    };
    if (VK_SUCCESS == vkCreateBuffer(info.device, &bufferCreateInfo, nullptr, &info.vertex_buffer.buf)) {
        std::cout<<"[MTKPGPU-APP] vertex buffer is created: "<<info.vertex_buffer.buf<<std::endl;
    } else {
        std::cout<<"[MTKPGPU-APP] something went wrong while creating vertex buffer"<<std::endl;
    }

    // bind memory to resource, basically to determine the buffer size (usage flag can be changed later)
    VkMemoryRequirements memoryRequirements;
    vkGetBufferMemoryRequirements(info.device, info.vertex_buffer.buf, &memoryRequirements);
    std::cout<<"[MTKPGPU-APP] vertex buf memory size: "<<memoryRequirements.size << std::endl;
    std::cout<<"[MTKPGPU-APP] vertex buf memory alignment: "<<memoryRequirements.alignment << std::endl;
    std::cout<<"[MTKPGPU-APP] vertex buf memory type: "<<memoryRequirements.memoryTypeBits<<std::endl;

    // allocate device memory
    /* Device memory in Vulkan refers to memory that is accessible
     * to the device and usable as a backing store for
     * textures and other data
     * */
    vkGetPhysicalDeviceMemoryProperties(info.gpus[0], &info.memory_properties);
    std::cout<<"[MTKPGPU-APP] device supports "<<info.memory_properties.memoryTypeCount<<" types of device memory"<<std::endl;
    std::cout<<"[MTKPGPU-APP] device supports "<<info.memory_properties.memoryHeapCount<<" types of device heap"<<std::endl;

    int32_t typeIndex = -1;
    for (int32_t i = 0, typeBits = memoryRequirements.memoryTypeBits; i < 32; ++i) {
        if ((typeBits & 1) == 1) {
            if ((info.memory_properties.memoryTypes[i].propertyFlags & VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT) == VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT) {
                typeIndex = i;
                break;
            }
        }
        typeBits >>= 1;
    }
    if (typeIndex != -1) {
        std::cout
                << "[MTKPGPU-APP] device's mem type supports VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT at index: "
                << typeIndex << std::endl;
    } else {
        std::cout<<"[MTKPGPU-APP] device's mem type doesn't include VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT"<<std::endl;
        return false;
    }

    // allocate memory
    VkMemoryAllocateInfo memoryAllocateInfo = {
            .sType = VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO,
            .pNext = nullptr,
            .allocationSize = memoryRequirements.size,
            .memoryTypeIndex = static_cast<uint32_t>(typeIndex),
    };
    if (VK_SUCCESS == vkAllocateMemory(info.device, &memoryAllocateInfo, nullptr, &(info.vertex_buffer.mem))) {
        std::cout<<"[MTKPGPU-APP] vertex buffer memory is allocated"<<std::endl;
    } else {
        std::cout<<"[MTKPGPU-APP] something went wrong while allocating vertex buffer memory"<<std::endl;
        return false;
    }

    // map device memory to host
    /* To map device memory into the host’s address space, the memory object to be mapped must have
     * been allocated from a heap that has the VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT flag
     * */
    void* data;
    if (VK_SUCCESS == vkMapMemory(info.device, info.vertex_buffer.mem, 0, sizeof(vertexData), 0, &data)) {
        memcpy(data, vertexData, sizeof(vertexData));
        vkUnmapMemory(info.device, info.vertex_buffer.mem);
    } else {
        std::cout<<"[MTKPGPU-APP] something went wrong while mapping vertex buffer memory to host"<<std::endl;
        return false;
    }

    /* Once you have chosen the memory type for the resource, you can bind a piece of a memory object to
       that resource by calling either vkBindBufferMemory() for buffer objects
     * */
    if (VK_SUCCESS == vkBindBufferMemory(info.device, info.vertex_buffer.buf, info.vertex_buffer.mem, 0)) {
        std::cout<<"[MTKPGPU-APP] vertex buffer memory is bound to buffer object"<<std::endl;
    } else {
        std::cout<<"[MTKPGPU-APP] something went wrong while binding vertex buffer memory to buffer object"<<std::endl;
        return false;
    }

    return true;
}

bool createGraphicsPipelines(struct sample_info &info, VkExtent2D &displaySize) {
    const VkPushConstantRange pushConstantRange = {
            .stageFlags = VK_SHADER_STAGE_FRAGMENT_BIT,
            .offset = 0,
            .size = 3 * sizeof(float),
    };
    const VkPipelineLayoutCreateInfo pipelineLayoutCreateInfo = {
            .sType = VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO,
            .pNext = nullptr,
            .flags = 0,
            .setLayoutCount = 0,
            .pSetLayouts = nullptr,
            .pushConstantRangeCount = 1,
            .pPushConstantRanges = &pushConstantRange,
    };
    if (VK_SUCCESS == vkCreatePipelineLayout(info.device, &pipelineLayoutCreateInfo, nullptr, &info.pipeline_layout)) {
        std::cout<<"[MTKPGPU-APP] pipeline layout is created"<<std::endl;
    } else {
        std::cout<<"[MTKPGPU-APP] something went wrong while creating pipeline layout"<<std::endl;
        return false;
    }

    //ASSERT(!loadShaderFromFile("shaders/tri.frag.spv", &mFragmentShader));

    if (!(vertShaderText || fragShaderText)) {
        std::cout<<"[MTKPGPU-APP] can not find shader code"<<std::endl;
        return false;
    }

    VkShaderModuleCreateInfo moduleCreateInfo;

    // load vertex shader
    if (vertShaderText) {
        std::vector<unsigned int> vtx_spv;
        info.shaderStages[0].sType = VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO;
        info.shaderStages[0].pNext = NULL;
        info.shaderStages[0].pSpecializationInfo = NULL;
        info.shaderStages[0].flags = 0;
        info.shaderStages[0].stage = VK_SHADER_STAGE_VERTEX_BIT;
        info.shaderStages[0].pName = "main";

        if (!GLSLtoSPV(VK_SHADER_STAGE_VERTEX_BIT, vertShaderText, vtx_spv)) {
            std::cout<<"[MTKPGPU-APP] can not load vertex shader code to spv format"<<std::endl;
            return false;
        } else {
            std::cout<<"[MTKPGPU-APP] vertex shader code is loaded and tranlate to spv format"<<std::endl;
        }

        moduleCreateInfo.sType = VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO;
        moduleCreateInfo.pNext = nullptr;
        moduleCreateInfo.flags = 0;
        moduleCreateInfo.codeSize = vtx_spv.size() * sizeof(unsigned int);
        moduleCreateInfo.pCode = vtx_spv.data();

        if (VK_SUCCESS == vkCreateShaderModule(info.device, &moduleCreateInfo, NULL, &info.shaderStages[0].module)) {
            std::cout<<"[MTKPGPU-APP] vertex shader module is created"<<std::endl;
        } else {
            std::cout<<"[MTKPGPU-APP] something went wrong while creating vertex shader module"<<std::endl;
            return false;
        }
    }

    if (fragShaderText) {
        std::vector<unsigned int> frag_spv;
        info.shaderStages[1].sType = VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO;
        info.shaderStages[1].pNext = NULL;
        info.shaderStages[1].pSpecializationInfo = NULL;
        info.shaderStages[1].flags = 0;
        info.shaderStages[1].stage = VK_SHADER_STAGE_FRAGMENT_BIT;
        info.shaderStages[1].pName = "main";

        if (!GLSLtoSPV(VK_SHADER_STAGE_FRAGMENT_BIT, fragShaderText, frag_spv)){
            std::cout<<"[MTKPGPU-APP] can not load pixel shader code to spv format"<<std::endl;
            return false;
        } else {
            std::cout<<"[MTKPGPU-APP] pixel shader code is loaded and tranlate to spv format"<<std::endl;
        }

        moduleCreateInfo.sType = VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO;
        moduleCreateInfo.pNext = NULL;
        moduleCreateInfo.flags = 0;
        moduleCreateInfo.codeSize = frag_spv.size() * sizeof(unsigned int);
        moduleCreateInfo.pCode = frag_spv.data();

        if (VK_SUCCESS == vkCreateShaderModule(info.device, &moduleCreateInfo, NULL, &info.shaderStages[1].module)) {
            std::cout<<"[MTKPGPU-APP] pixel shader module is created"<<std::endl;
        } else {
            std::cout<<"[MTKPGPU-APP] something went wrong while creating pixel shader module"<<std::endl;
            return false;
        }
    }

    const VkViewport viewports = {
            .x = 0.0f,
            .y = 0.0f,
            .width = (float)displaySize.width,
            .height = (float)displaySize.height,
            .minDepth = 0.0f,
            .maxDepth = 1.0f,
    };
    const VkRect2D scissor = {
            .offset = {
                .x = 0,
                .y = 0,
            },
            .extent = displaySize,
    };
    const VkPipelineViewportStateCreateInfo viewportInfo = {
            .sType = VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO,
            .pNext = nullptr,
            .flags = 0,
            .viewportCount = 1,
            .pViewports = &viewports,
            .scissorCount = 1,
            .pScissors = &scissor,
    };
    VkSampleMask sampleMask = ~0u;
    const VkPipelineMultisampleStateCreateInfo multisampleInfo = {
            .sType = VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO,
            .pNext = nullptr,
            .flags = 0,
            .rasterizationSamples = VK_SAMPLE_COUNT_1_BIT,
            .sampleShadingEnable = VK_FALSE,
            .minSampleShading = 0,
            .pSampleMask = &sampleMask,
            .alphaToCoverageEnable = VK_FALSE,
            .alphaToOneEnable = VK_FALSE,
    };
    const VkPipelineColorBlendAttachmentState attachmentStates = {
            .blendEnable = VK_FALSE,
            .srcColorBlendFactor = (VkBlendFactor)0,
            .dstColorBlendFactor = (VkBlendFactor)0,
            .colorBlendOp = (VkBlendOp)0,
            .srcAlphaBlendFactor = (VkBlendFactor)0,
            .dstAlphaBlendFactor = (VkBlendFactor)0,
            .alphaBlendOp = (VkBlendOp)0,
            .colorWriteMask = VK_COLOR_COMPONENT_R_BIT | VK_COLOR_COMPONENT_G_BIT |
                              VK_COLOR_COMPONENT_B_BIT | VK_COLOR_COMPONENT_A_BIT,
    };
    const VkPipelineColorBlendStateCreateInfo colorBlendInfo = {
            .sType = VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO,
            .pNext = nullptr,
            .flags = 0,
            .logicOpEnable = VK_FALSE,
            .logicOp = VK_LOGIC_OP_COPY,
            .attachmentCount = 1,
            .pAttachments = &attachmentStates,
            .blendConstants = {0.0f, 0.0f, 0.0f, 0.0f},
    };
    const VkPipelineRasterizationStateCreateInfo rasterInfo = {
            .sType = VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO,
            .pNext = nullptr,
            .flags = 0,
            .depthClampEnable = VK_FALSE,
            .rasterizerDiscardEnable = VK_FALSE,
            .polygonMode = VK_POLYGON_MODE_FILL,
            .cullMode = VK_CULL_MODE_NONE,
            .frontFace = VK_FRONT_FACE_CLOCKWISE,
            .depthBiasEnable = VK_FALSE,
            .depthBiasConstantFactor = 0,
            .depthBiasClamp = 0,
            .depthBiasSlopeFactor = 0,
            .lineWidth = 1,
    };
    const VkPipelineInputAssemblyStateCreateInfo inputAssemblyInfo = {
            .sType = VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO,
            .pNext = nullptr,
            .flags = 0,
            .topology = VK_PRIMITIVE_TOPOLOGY_TRIANGLE_STRIP,
            .primitiveRestartEnable = VK_FALSE,
    };
    const VkVertexInputBindingDescription vertexInputBindingDescription = {
            .binding = 0,
            .stride = 3 * sizeof(float),
            .inputRate = VK_VERTEX_INPUT_RATE_VERTEX,
    };
    const VkVertexInputAttributeDescription vertexInputAttributeDescription = {
            .location = 0,
            .binding = 0,
            .format = VK_FORMAT_R32G32B32_SFLOAT,
            .offset = 0,
    };
    const VkPipelineVertexInputStateCreateInfo vertexInputInfo = {
            .sType = VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO,
            .pNext = nullptr,
            .flags = 0,
            .vertexBindingDescriptionCount = 1,
            .pVertexBindingDescriptions = &vertexInputBindingDescription,
            .vertexAttributeDescriptionCount = 1,
            .pVertexAttributeDescriptions = &vertexInputAttributeDescription,
    };
    const VkGraphicsPipelineCreateInfo pipelineCreateInfo = {
            .sType = VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO,
            .pNext = nullptr,
            .flags = 0,
            .stageCount = 2,
            .pStages = info.shaderStages,
            .pVertexInputState = &vertexInputInfo,
            .pInputAssemblyState = &inputAssemblyInfo,
            .pTessellationState = nullptr,
            .pViewportState = &viewportInfo,
            .pRasterizationState = &rasterInfo,
            .pMultisampleState = &multisampleInfo,
            .pDepthStencilState = nullptr,
            .pColorBlendState = &colorBlendInfo,
            .pDynamicState = nullptr,
            .layout = info.pipeline_layout,
            .renderPass = info.render_pass,
            .subpass = 0,
            .basePipelineHandle = VK_NULL_HANDLE,
            .basePipelineIndex = 0,
    };

    if (VK_SUCCESS == vkCreateGraphicsPipelines(info.device, VK_NULL_HANDLE, 1, &pipelineCreateInfo, nullptr, &info.pipeline)) {
        std::cout<<"[MTKPGPU-APP] graphics pipeline is created"<<std::endl;
    } else {
        std::cout<<"[MTKPGPU-APP] something went wrong while creaing graphics pipeline"<<std::endl;
        return false;
    }

    vkDestroyShaderModule(info.device, info.shaderStages[0].module, nullptr);
    vkDestroyShaderModule(info.device, info.shaderStages[1].module, nullptr);
    info.shaderStages[0].module = VK_NULL_HANDLE;
    info.shaderStages[1].module = VK_NULL_HANDLE;

    return true;
}

bool executeCommandBuffer(struct sample_info &info, std::vector<VkCommandBuffer> &commandBuffers, VkExtent2D &displaySize) {

    // we expected the swapchain length is 2
    for (uint32_t i = 0; i < info.swapchainImageCount; ++i) {
        const VkCommandBufferBeginInfo commandBufferBeginInfo = {
                .sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO,
                .pNext = nullptr,
                .flags = 0,
                .pInheritanceInfo = nullptr,
        };
        //vkBeginCommandBuffer(info.cmd, &commandBufferBeginInfo);
        std::cout<< "[MTKPGPU-APP] begin command buffer "<< i << " : "<<commandBuffers[i]<<std::endl;
        if (VK_SUCCESS == vkBeginCommandBuffer(commandBuffers[i], &commandBufferBeginInfo)) {
            std::cout << "[MTKPGPU-APP] begin command buffer successful for buffer: "<< i <<std::endl;
        } else {
            std::cout << "[MTKPGPU-APP] begin command buffer failed for buffer: "<< i <<std::endl;
            return false;
        }

        const VkClearValue clearVals = {
                .color.float32[0] = 0.0f,
                .color.float32[1] = 0.0f,
                .color.float32[2] = 0.0f,
                .color.float32[3] = 1.0f,
        };
        const VkRenderPassBeginInfo renderPassBeginInfo = {
                .sType = VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO,
                .pNext = nullptr,
                .renderPass = info.render_pass,
                .framebuffer = info.framebuffers[i],
                .renderArea = {
                    .offset = {
                        .x = 0,
                        .y = 0,
                    },
                    .extent = displaySize,
                },
                .clearValueCount = 1,
                .pClearValues = &clearVals,
        };
        vkCmdBeginRenderPass(commandBuffers[i], &renderPassBeginInfo, VK_SUBPASS_CONTENTS_INLINE);

        /* Before you can use a pipeline, it must be bound into the a command buffer that will execute drawing
         * or dispatching commands.
         */
        vkCmdBindPipeline(commandBuffers[i], VK_PIPELINE_BIND_POINT_GRAPHICS, info.pipeline);

        VkDeviceSize offset = 0;
        vkCmdBindVertexBuffers(commandBuffers[i], 0, 1, &info.vertex_buffer.buf, &offset);
        vkCmdPushConstants(commandBuffers[i], info.pipeline_layout, VK_SHADER_STAGE_FRAGMENT_BIT, 0, 3 * sizeof(float), &fragData[0]);
        vkCmdDraw(commandBuffers[i], 4, 1, 0, 0);

        vkCmdPushConstants(commandBuffers[i], info.pipeline_layout, VK_SHADER_STAGE_FRAGMENT_BIT, 0, 3 * sizeof(float), &fragData[3]);
        vkCmdDraw(commandBuffers[i], 4, 1, 2, 0);

        vkCmdPushConstants(commandBuffers[i], info.pipeline_layout, VK_SHADER_STAGE_FRAGMENT_BIT, 0, 3 * sizeof(float), &fragData[6]);
        vkCmdDraw(commandBuffers[i], 4, 1, 6, 0);

        vkCmdPushConstants(commandBuffers[i], info.pipeline_layout, VK_SHADER_STAGE_FRAGMENT_BIT, 0, 3 * sizeof(float), &fragData[9]);
        vkCmdDraw(commandBuffers[i], 4, 1, 8, 0);

        vkCmdEndRenderPass(commandBuffers[i]);

        if (VK_SUCCESS == vkEndCommandBuffer(commandBuffers[i])) {
            std::cout << "[MTKPGPU-APP] end command buffer successful for buffer: "<< i <<std::endl;
        } else {
            std::cout << "[MTKPGPU-APP] end command buffer failed for buffer: "<< i <<std::endl;
            return false;
        }
    }

    return true;
}

bool createFenceAndSemaphore(struct sample_info &info, VkFence &fence, VkSemaphore &semaphore) {
    const VkFenceCreateInfo fenceCreateInfo = {
            .sType = VK_STRUCTURE_TYPE_FENCE_CREATE_INFO,
            .pNext = nullptr,
            .flags = 0,
    };
    if (VK_SUCCESS == vkCreateFence(info.device, &fenceCreateInfo, nullptr, &fence)) {
        std::cout << "[MTKPGPU-APP] fence is created" <<std::endl;
    } else {
        std::cout << "[MTKPGPU-APP] something went wrong while creating fence" <<std::endl;
        return false;
    }

    const VkSemaphoreCreateInfo semaphoreCreateInfo = {
            .sType = VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO,
            .pNext = nullptr,
            .flags = 0,
    };
    if (VK_SUCCESS == vkCreateSemaphore(info.device, &semaphoreCreateInfo, nullptr, &semaphore)) {
        std::cout << "[MTKPGPU-APP] semaphore is created" <<std::endl;
    } else {
        std::cout << "[MTKPGPU-APP] something went wrong while creating semaphore" <<std::endl;
        return false;
    }
    return true;
}

bool drawOneFrame(struct sample_info &info, std::vector<VkCommandBuffer> &commandBuffers, VkFence &fence, VkSemaphore &semaphore) {
    uint32_t nextIndex;

    if (VK_SUCCESS == vkAcquireNextImageKHR(info.device, info.swap_chain, UINT64_MAX, semaphore, VK_NULL_HANDLE, &nextIndex)) {
        std::cout << "[MTKPGPU-APP] acquired the next image"<<std::endl;
    } else {
        std::cout << "[MTKPGPU-APP] something went wrong while acquiring the next image"<<std::endl;
        return false;
    }

    if (VK_SUCCESS == vkResetFences(info.device, 1, &fence)){
        std::cout << "[MTKPGPU-APP] reset the fence"<<std::endl;
    } else {
        std::cout << "[MTKPGPU-APP] something went wrong while resetting the fence"<<std::endl;
        return false;
    }

    // drawing
    VkPipelineStageFlags waitStageMask = VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;
    const VkSubmitInfo submitInfo = {
            .sType = VK_STRUCTURE_TYPE_SUBMIT_INFO,
            .pNext = nullptr,
            .waitSemaphoreCount = 1,
            .pWaitSemaphores = &semaphore,
            .pWaitDstStageMask = &waitStageMask,
            .commandBufferCount = 1,
            .pCommandBuffers = &commandBuffers[nextIndex],
            .signalSemaphoreCount = 0,
            .pSignalSemaphores = nullptr,
    };
    std::cout<<"Swapchain before submit queue: "<<info.swap_chain<<std::endl;
    if(VK_SUCCESS == vkQueueSubmit(info.graphics_queue, 1, &submitInfo, fence)) {
        std::cout<< "[MTKPGPU-APP] submitted command to queue (gfx queue)" << std::endl;
    } else {
        std::cout<< "[MTKPGPU-APP] something went wrong while submitting command to queue (gfx queue)" << std::endl;
        return false;
    }
    std::cout<<"Swapchain after submit queue, before fence signal: "<<info.swap_chain<<std::endl;
    if (VK_SUCCESS == vkWaitForFences(info.device, 1, &fence, VK_TRUE, 100000000)) {
        std::cout<< "[MTKPGPU-APP] drawing is completed" << std::endl;
    } else {
        std::cout<< "[MTKPGPU-APP] something went wrong while waiting for fence" << std::endl;
        return false;
    }
    std::cout<<"Swapchain after fence signal: "<<info.swap_chain<<std::endl;

    // displaying
    const VkSwapchainKHR swapchain = info.swap_chain;
    const VkPresentInfoKHR presentInfo = {
            .sType = VK_STRUCTURE_TYPE_PRESENT_INFO_KHR,
            .pNext = nullptr,
            .waitSemaphoreCount = 0,
            .pWaitSemaphores = nullptr,
            .swapchainCount = 1,
            .pSwapchains = &swapchain,
            .pImageIndices = &nextIndex,
            .pResults = nullptr,
    };
    if (VK_SUCCESS == vkQueuePresentKHR(info.present_queue, &presentInfo)) {
        std::cout<< "[MTKPGPU-APP] frame is displayed" << std::endl;
    } else {
        std::cout<< "[MTKPGPU-APP] something went wrong while display one frame" << std::endl;
        return false;
    }

    return true;
}

int sample_main(int argc, char *argv[]) {

    VkSurfaceCapabilitiesKHR surfCapabilities;
    std::vector<VkCommandBuffer> commandBuffers;
    VkExtent2D displaySize;
    VkSemaphore semaphore;
    VkFence fence;

    struct sample_info info = {};
    init_global_layer_properties(info);
    init_instance_extension_names(info);
    init_device_extension_names(info);
    init_instance(info, "vulkansamples_enumerate");

    checkInstAndDevExtensions(info);
    createAndroidSurface(info);
    checkSurfaceCapabilities(info, surfCapabilities);

    if (!checkQueueSupportPresent(info)) return -1;
    if (!createDevice(info)) return -1;
    if (!obtainQueue(info)) return -1;
    if (!createSwapchain(info, surfCapabilities, displaySize, /*rotation*/ true)) return -1;
    if (!createCommandPool(info)) return -1;
    if (!createCommandBuffer(info, commandBuffers)) return -1;

    if (!createRenderPass(info)) return -1;
    if (!createFramebuffers(info, displaySize)) return -1;

    if (!createVertexBuffers(info)) return -1;
    if (!createGraphicsPipelines(info, displaySize)) return -1;

    if (!executeCommandBuffer(info, commandBuffers, displaySize)) return -1;
    if (!createFenceAndSemaphore(info, fence, semaphore)) return -1;

    for (uint32_t i = 0; i < 120; ++i) {
        if (!drawOneFrame(info, commandBuffers, fence, semaphore)) {
            std::cout << "[MTKPGPU-APP] something went wrong while drawing frame " << i << std::endl;
            return -1;

        } else {
            std::cout << "[MTKPGPU-APP] frame "<< i << " is drawn" << std::endl;
        }
    }
    /* VULKAN_KEY_END */

    return 0;
}
