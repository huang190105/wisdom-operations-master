/*
 * Copyright (c) [2018]
 * This file is part of the java-wisdomcore
 *
 * The java-wisdomcore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The java-wisdomcore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the java-wisdomcore. If not, see <http://www.gnu.org/licenses/>.
 */

package com.wisdom.monitor.model;

import com.google.common.primitives.Bytes;
import com.wisdom.monitor.utils.Utils;
import com.wisdom.monitor.utils.crypto.AESManage;
import com.wisdom.monitor.utils.crypto.ArgonManage;
import com.wisdom.monitor.utils.crypto.SHA3Utility;
import com.wisdom.monitor.utils.crypto.ed25519.Ed25519;
import com.wisdom.monitor.utils.crypto.ed25519.Ed25519KeyPair;
import com.wisdom.monitor.utils.crypto.ed25519.Ed25519PublicKey;
import org.apache.commons.codec.binary.Hex;


import java.security.SecureRandom;

public class Keystore {

    public static Keystore newInstance(String password) throws Exception {
        if (password.length() > 20 || password.length() < 8) {
            throw new Exception("请输入8-20位密码");
        } else {
            Ed25519KeyPair keyPair = Ed25519.generateKeyPair();
            Ed25519PublicKey publicKey = keyPair.getPublicKey();
            String s = new String(keyPair.getPrivateKey().getEncoded());
            byte[] salt = new byte[saltLength];
            byte[] iv = new byte[ivLength];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            SecureRandom sr = new SecureRandom();
            sr.nextBytes(salt);
            ArgonManage argon2id = new ArgonManage(ArgonManage.Type.ARGON2id, salt);
            AESManage aes = new AESManage(iv);

            byte[] derivedKey = argon2id.hash(password.getBytes());
            byte[] cipherPrivKey = aes.encrypt(derivedKey, keyPair.getPrivateKey().getEncoded());
            byte[] mac = SHA3Utility.keccak256(Bytes.concat(
                    derivedKey, cipherPrivKey
                    )
            );

            Crypto crypto = new Crypto(
                    AESManage.cipher, Hex.encodeHexString(cipherPrivKey),
                    new Cipherparams(
                            Hex.encodeHexString(iv)
                    )
            );
            Kdfparams kdfparams = new Kdfparams(ArgonManage.memoryCost, ArgonManage.timeCost, ArgonManage.parallelism, Hex.encodeHexString(salt));

            Address ads = new Address(publicKey);
            ArgonManage params = new ArgonManage(salt);
            Keystore ks = new Keystore(ads.getAddress(), crypto, Utils.generateUUID(),
                    defaultVersion, Hex.encodeHexString(mac), argon2id.kdf(), kdfparams
            );
            return ks;
        }
    }


    public String address;
    public Crypto crypto;
    public Kdfparams kdfparams;
    public String id;
    public String version;
    public String mac;
    public String kdf;
    private static final int saltLength = 32;
    private static final int ivLength = 16;
    private static final String defaultVersion = "1";


    public Keystore(String address, Crypto crypto, String id, String version, String mac, String kdf, Kdfparams kdfparams) {
        this.address = address;
        this.crypto = crypto;
        this.id = id;
        this.version = version;
        this.mac = mac;
        this.kdf = kdf;
        this.kdfparams = kdfparams;

    }

    public Keystore() {
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Crypto getCrypto() {
        return crypto;
    }

    public void setCrypto(Crypto crypto) {
        this.crypto = crypto;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getKdf() {
        return kdf;
    }

    public void setKdf(String kdf) {
        this.kdf = kdf;
    }

    public Kdfparams getKdfparams() {
        return kdfparams;
    }

    public void setKdfparams(Kdfparams kdfparams) {
        this.kdfparams = kdfparams;
    }
}