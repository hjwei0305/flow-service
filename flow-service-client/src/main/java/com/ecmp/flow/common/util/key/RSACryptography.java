package com.ecmp.flow.common.util.key;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * *************************************************************************************************
 * <p>
 * 实现功能：RSA工具方法
 * </p>
 * ------------------------------------------------------------------------------------------------
 * 版本          变更时间             变更人                     变更原因
 * ------------------------------------------------------------------------------------------------
 * 1.0.00      2017/9/25 15:50      谭军(tanjun)                    新建
 * </p><p>
 * *************************************************************************************************
 * </p>
 */
public class RSACryptography {

//    public static void main(String[] args) throws Exception {
////        String data="hello world";
////
////        KeyPair keyPair=genKeyPair(2048);
////
////        //获取公钥，并以base64格式打印出来
////        PublicKey publicKey=keyPair.getPublic();
////        System.out.println("公钥："+new String(Base64.getEncoder().encode(publicKey.getEncoded())));
////
////        //获取私钥，并以base64格式打印出来
////        PrivateKey privateKey=keyPair.getPrivate();
////        System.out.println("私钥："+new String(Base64.getEncoder().encode(privateKey.getEncoded())));
//
////        //公钥加密
////        byte[] encryptedBytes=encrypt(data.getBytes(), publicKey);
////        System.out.println("加密后："+new String(encryptedBytes));
////
////        //私钥解密
////        byte[] decryptedBytes=decrypt(encryptedBytes, privateKey);
////        System.out.println("解密后："+new String(decryptedBytes));
//        String addTokenStr = "&userAccount=111111&cc=中文";
//        PrivateKey privateKey = RSACryptography.getPrivateKey(Constants.SELFPRIVATEKEY);
//        PublicKey  publicKey = RSACryptography.getPublicKey(Constants.SELFPUBKEY);
//        String addTokenParamsSecret = new String(RSACryptography.encrypt(addTokenStr.getBytes("iso8859-1"),privateKey),"iso8859-1");
//        addTokenParamsSecret = URLEncoder.encode(addTokenParamsSecret, "iso8859-1");
//        String addTokenParamsStr = URLDecoder.decode(addTokenParamsSecret,"iso8859-1");
////        String addTokenParamsStr = addTokenParamsSecret;
//
//        String resultStr = new String(RSACryptography.decrypt(addTokenParamsStr.getBytes("iso8859-1"),publicKey),"iso8859-1");
//        System.out.println(resultStr);
////        //私钥加密
////        byte[] encryptedBytes=encrypt(addTokenStr.getBytes(), privateKey);
////        System.out.println("加密后："+new String(encryptedBytes,"iso8859-1"));
////        System.out.println(encryptedBytes.length);
////        System.out.println(new String(encryptedBytes,"iso8859-1").getBytes("iso8859-1").length);
////        //公钥解密
////        byte[] decryptedBytes=decrypt(new String(encryptedBytes,"iso8859-1").getBytes("iso8859-1"),publicKey );
////        System.out.println("解密后："+new String(decryptedBytes));
//
//
//    }

    //生成密钥对
    public static KeyPair genKeyPair(int keyLength) throws Exception{
        KeyPairGenerator keyPairGenerator=KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
    }

    //公钥加密
    public static byte[] encrypt(byte[] content, PublicKey publicKey) throws Exception{
        Cipher cipher=Cipher.getInstance("RSA");//java默认"RSA"="RSA/ECB/PKCS1Padding"
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(content);
    }

    //私钥解密
    public static byte[] decrypt(byte[] content, PrivateKey privateKey) throws Exception{
        Cipher cipher=Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(content);
    }

    //私钥加密
    public static byte[] encrypt(byte[] content,  PrivateKey privateKey) throws Exception{
        Cipher cipher=Cipher.getInstance("RSA");//java默认"RSA"="RSA/ECB/PKCS1Padding"
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        return cipher.doFinal(content);
    }

    //公钥解密
    public static byte[] decrypt(byte[] content, PublicKey publicKey) throws Exception{
        Cipher cipher=Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        return cipher.doFinal(content);
    }

    //将base64编码后的公钥字符串转成PublicKey实例
    public static PublicKey getPublicKey(String publicKey) throws Exception{
        byte[ ] keyBytes=Base64.getDecoder().decode(publicKey.getBytes());
        X509EncodedKeySpec keySpec=new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory=KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    //将base64编码后的私钥字符串转成PrivateKey实例
    public static PrivateKey getPrivateKey(String privateKey) throws Exception{
        byte[ ] keyBytes=Base64.getDecoder().decode(privateKey.getBytes());
        PKCS8EncodedKeySpec keySpec=new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory=KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }
}
