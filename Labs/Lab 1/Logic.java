package Lab1;

import java.awt.Dimension;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.security.*;
import java.io.*;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.swing.*;

public class Logic implements ActionListener {
	private JFrame frame;
	private JPanel panel;
	private JTextField input;
	private JButton encrypt;
	private JButton decrypt;
	private KeyPair keyPair;
	private SecretKey cryptoKey;
	private Signature signature;
	private byte[] signatureBytes;
	
	public Logic() throws NoSuchAlgorithmException, NoSuchPaddingException {
		keyPair = getKeyPair();
		//cryptoKey = getKeyPair();
		KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
		cryptoKey = keyGenerator.generateKey();
		// Frame setup
		frame = new JFrame("CSI 4139 - Lab 1");
		frame.setVisible(true);
		//Panel Setup
		panel = new JPanel();
		//TextField Setup
		input = new JTextField(50);
		// crate signature and apply to file
		signature = Signature.getInstance("SHA1WithRSA");
		//Encryption button setup
		encrypt = new JButton("Encrypt");
		encrypt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Encrypt");
				try {
					encryptSign(input.getText());
				} catch (InvalidKeyException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (NoSuchAlgorithmException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (SignatureException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (NoSuchPaddingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IllegalBlockSizeException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (BadPaddingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		//Decryption button setup
		decrypt = new JButton("Decrypt");
		decrypt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Decrypt");
				try {
					decryptSign(input.getText());
				} catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException | IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (NoSuchPaddingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IllegalBlockSizeException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (BadPaddingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		//Put everything together
		panel.add(input);
		panel.add(encrypt);
		panel.add(decrypt);
		frame.add(panel);
		frame.pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) { }
	
	private boolean encryptSign(String filePath) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		// getting plain text file
		File file = new File(filePath);

		byte[] data = new byte[(int) file.length()];
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			fileInputStream.read(data);
			for (int i = 0; i < data.length; i++) {
				System.out.print((char) data[i]);
			}
		} catch (FileNotFoundException e) {
			System.out.println("File Not Found.");
			e.printStackTrace();
		} catch (IOException e1) {
			System.out.println("Error Reading The File.");
			e1.printStackTrace();
		}

		signature.initSign(keyPair.getPrivate());
		signature.update(data);

		signatureBytes = signature.sign();
		System.out.println("Signature:" + Base64.getEncoder().encode(signatureBytes));
		
		//Encrypt the file
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, cryptoKey);
		byte[] encryptedData = cipher.doFinal(data);

		// save signed file as new file
		FileOutputStream stream = new FileOutputStream(filePath);
		try {
			stream.write(encryptedData);
		} finally {
			stream.close();
		}
		return true;
	}
	
	private boolean decryptSign(String filePath) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		 File file = new File(filePath);

	        byte[] data = new byte[(int) file.length()];
	        try {
	              FileInputStream fileInputStream = new FileInputStream(file);
	              fileInputStream.read(data);
	              for (int i = 0; i < data.length; i++) {
	                          System.out.print((char)data[i]);
	               }
	         } catch (FileNotFoundException e) {
	                     System.out.println("File Not Found.");
	                     e.printStackTrace();
	         }
	         catch (IOException e1) {
	                  System.out.println("Error Reading The File.");
	                   e1.printStackTrace();
	         }
	        
	      //Encrypt the file
			Cipher cipher = Cipher.getInstance("AES");
			
			cipher.init(Cipher.DECRYPT_MODE, cryptoKey);
			byte[] decryptedData = cipher.doFinal(data);
	        
	        //verify signed file
	        signature.initVerify(keyPair.getPublic());
	        signature.update(decryptedData);
	        System.out.println(signature.verify(signatureBytes));
	        
	        FileOutputStream stream = new FileOutputStream(filePath);
			try {
				stream.write(decryptedData);
			} finally {
				stream.close();
			}
	        
		return true;
	}
	
	private KeyPair getKeyPair() throws NoSuchAlgorithmException {
		KeyPairGenerator keyPair = KeyPairGenerator.getInstance("RSA");
		keyPair.initialize(1024);
		return keyPair.genKeyPair();
	}
	
}
