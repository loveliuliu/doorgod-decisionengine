/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.doorgod.decisionengine;

import com.ymatou.doorgod.decisionengine.config.props.BizProps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author luoshiqian 2016/9/28 14:20
 */
@Component
public class HttpServer implements ApplicationListener {

    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);
    @Autowired
    private BizProps bizProps;

    public void start(int port) {
        try {
            ServerSocket ss= new ServerSocket(port);

            while(true){
                try {
                    Socket socket=ss.accept();
                    BufferedReader bd=new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    /**
                     * 接受HTTP请求
                     */
                    String requestHeader;
                    String requestUrl = "";
                    while((requestHeader=bd.readLine())!=null&&!requestHeader.isEmpty()){
                        if(requestHeader.contains("warmup")){
                            requestUrl = "warmup";
                        }
                    }

                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8"));

                    String str = " \n"+
                            "HTTP/1.1 200 OK \n" +
                            "Content-type:text/plain \n" +
                            "\n";
                    if(requestUrl.equals("warmup")){
                        str += "ok";
                    }else {
                        str += "version: 2016-09-28 first version";
                    }
                    out.write(str);
                    out.flush();
                    out.close();
                    socket.close();
                } catch (Exception e) {
                    logger.error("execption",e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("启动httpServer失败",e);
        }
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {

        if (event instanceof ApplicationReadyEvent) {
            Thread thread = new Thread(() -> {
                this.start(bizProps.getPort());
            },"httpServer");
            thread.setDaemon(true);
            thread.start();
        }
    }
}