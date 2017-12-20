import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Random;
import java.io.*;
import java.lang.*;
import java.nio.*;
import java.nio.file.StandardOpenOption;
public class RsaKeyGen 

{
   public static void main(String[]args)throws IOException
   {
       RsaKeyGen();
   }
   public static void RsaKeyGen() throws IOException 
   {
    //Generate p values with given large integer constructer, the size of 512 (as specified in project description)   
    int x = 256;
    Random rnd = new Random();
   LargeInteger p = new LargeInteger(x, rnd);
    LargeInteger q = new LargeInteger(x, rnd);
     
     

     
     //Generate n value n = p*q
     LargeInteger n = p.multiply(q); 
     //Generate largeInteger with value 1
     byte[] o = {(byte) 0x01};
     LargeInteger one = new LargeInteger(o);
     //Generate phi(n) value phi(n) = (p-1)*(q-1)
     LargeInteger y = (p.subtract(one)).multiply(q.subtract(one)); 
     //Resize LargeInteger with value of 1 to be size of phi of n so when it is compared to phi(n) it is compared correctly
     byte [] s = new byte[y.length()];
     LargeInteger size = new LargeInteger(s);
     one = one.add(size);
     
     //Generate initial e value to start at 1
     byte[] startE = {(byte)0x01};
     LargeInteger e = new LargeInteger(startE);
     
     //add 1 to e until it meets condition GCD(y,e) = 1 at which case it should also meet the condition of being 1 < e < y 
     do{
         e = e.add(one);
        }
     while( (y.XGCD(e)[0]).compareTo(one) != 0);
  
     
     //generate d value which is in the array index 2 of the array returned by XGCD
     LargeInteger d = y.XGCD(e)[2];
     Path pubkey = Paths.get("pubkey.rsa");
     Files.write(pubkey, n.getVal());
     
     Files.write(pubkey, d.getVal(), StandardOpenOption.APPEND);
     
     Path privkey = Paths.get("privkey.rsa");
     Files.write(privkey, n.getVal());
     Files.write(privkey, d.getVal(), StandardOpenOption.APPEND );
     
     //Store e and n values in pubKey LargeInteger array
     LargeInteger[] pubKey = {e,n};
     //Store d and n values in privKey LargeInteger array
     LargeInteger[] privKey = {d,n};
     
     
    //For testing purposes 
    System.out.println("p: ");
    p.printArray();
    System.out.println(" q: ");
    q.printArray();
    System.out.println(" n: ");
    n.printArray();
    System.out.println(" y: ");
    y.printArray();
    System.out.println(" e: ");
    e.printArray();
    System.out.println(" d: ");
    d.printArray(); 
     
    }
}
