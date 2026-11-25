package com.xhzb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

/**
 * 启动程序
 *
 * @author XHZB
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class XHZBApplication
{
    public static void main(String[] args)
    {
        // System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(XHZBApplication.class, args);

        System.out.println("(♥◠‿◠)ﾉﾞ  XHZB 启动成功   ლ(´ڡ`ლ)ﾞ  \n" +
                "  __  __ _______  _____  ____  \n" +
                "  \\ \\/ / | | | | |___ / | __ ) \n" +
                "   \\  /  | |_| |   / /  |  _ \\ \n" +
                "   /  \\  |  _  |  / /__ | |_) |\n" +
                "  /_/\\_\\ |_| |_| /____ ||____/ \n" +
                "                                ");
    }
}