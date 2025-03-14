////package com.inje.pilly.config;
////
////import com.google.cloud.vision.v1.ImageAnnotatorClient;
////import com.google.cloud.vision.v1.ImageAnnotatorSettings;
////import org.springframework.context.annotation.Bean;
////import org.springframework.context.annotation.Configuration;
////
////import java.io.IOException;
////
////@Configuration
////public class OcrConfig {
////    @Bean
////    public ImageAnnotatorClient imageAnnotatorClient() {
////        try {
////            // 기본 설정을 사용하여 ImageAnnotatorClient 생성
////            ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder()
////                    .build();  // 기본 설정을 그대로 사용
////
////            return ImageAnnotatorClient.create(settings);
////        } catch (IOException e) {
////            throw new RuntimeException("Failed to create ImageAnnotatorClient", e);
////        }
//////        try {
//////            return ImageAnnotatorClient.create();
//////        } catch (IOException e) {
//////            // 예외 처리
//////            throw new RuntimeException("Failed to create ImageAnnotatorClient", e);
//////        }
////    }
////}
//package com.inje.pilly.config;
//
//import com.google.api.gax.grpc.GrpcTransportChannel;
//import com.google.api.gax.rpc.FixedTransportChannelProvider;
//import com.google.api.gax.rpc.TransportChannelProvider;
//import com.google.cloud.vision.v1.ImageAnnotatorClient;
//import com.google.cloud.vision.v1.ImageAnnotatorSettings;
//import io.grpc.ManagedChannel;
//import io.grpc.ManagedChannelBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.io.IOException;
//
//@Configuration
//public class OcrConfig {
//    @Bean
//    public ImageAnnotatorClient imageAnnotatorClient() {
//        try {
//            // gRPC 채널을 명시적으로 설정
//            ManagedChannel channel = ManagedChannelBuilder.forTarget("dns:///vision.googleapis.com")
//                    .defaultLoadBalancingPolicy("round_robin")
//                    .build();
//
//            TransportChannelProvider channelProvider =
//                    FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));
//
//            ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder()
//                    .setTransportChannelProvider(channelProvider)
//                    .build();
//
//            return ImageAnnotatorClient.create(settings);
//        } catch (IOException e) {
//            throw new RuntimeException("Failed to create ImageAnnotatorClient", e);
//        }
//    }
//}