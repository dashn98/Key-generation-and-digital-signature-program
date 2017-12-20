

import java.io.*;
import java.security.MessageDigest;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;

public class RsaSign
{
    public static void main(String[] args)
    {
        //Checks to make sure two many arguments aren't entered
        if(args.length != 2)
        {
            System.err.println("Invalid number of command line arguments");
            System.exit(0);
        }
        
        //Sets first argument to be the flag
        String flag = args[0];
        //Sets the second argument to be the file
        Path newFile = Paths.get(args[1]);
        //Checks whether the user wants to sign or verify file, also prints error if an incorrect argument is written
        if(flag.equalsIgnoreCase("s"))
        {
            sign(newFile);
        }
        else if(flag.equalsIgnoreCase("v"))
        {
            verify(newFile);
        }
        else
        {
            System.err.println("Invalid command line arguments (must enter 's' or 'v')");
            System.exit(0);
        }
    }
    
    public static void sign(Path newFile)
    {
       try{
           
            byte[] data = Files.readAllBytes(newFile);

            //create class instance to create SHA-256 hash
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            //process the file
            md.update(data);
            //generate a hash of the file
            byte[] digest = md.digest();
            //creates large integer with the hash value
            LargeInteger answer = new LargeInteger(digest);
            //Print signature for testing
            System.out.println("Signature");
            answer.printArray();
     
            
             Path privkey = Paths.get("privkey.rsa");
             byte[]bytes = Files.readAllBytes(privkey);
             
               byte[]n = new byte[64];
             for(int i = 0; i< n.length; i++)
             {
                 n[i] = bytes[i];
             }
             byte[]d = new byte[bytes.length-64];
             int j = 0;
             for(int i = n.length; i < bytes.length; i++)
             {
                 d[j] = bytes[i];
                 j++;
             }
           
          
             
             //Create N and D LargeInteger's with n and d byte arrays
              LargeInteger D = new LargeInteger(d);
              LargeInteger N = new LargeInteger(n);
           
            
            //Decrypt Hashed file using formula hash^(d%n)
            LargeInteger decrypted = answer.modularExp(D, N); 
           Path decrypt = Paths.get(newFile+".sig");
           Files.write(decrypt, decrypted.getVal()); 
            
            //Print Decrypted, D and N for testing purposes
            System.out.println("N ");
            N.printArray();
            System.out.println("D ");
            D.printArray();
            System.out.println("Decrypted");
            decrypted.printArray();
      
        
       
            
        }
         catch(Exception e)
         {
            System.out.println("error reading from file");
            System.out.println(e.toString());
         }
    }

    public static void verify(Path newFile)
    {
        try
        {
         Path yo = Paths.get(newFile+"");
         byte[]data = Files.readAllBytes(yo); 
          
         Path yo2 = Paths.get(newFile+".sig");
         byte[]decrypt = Files.readAllBytes(yo2);
           
          //Create LargeInteger with decrypted value read from file
          LargeInteger decrypted = new LargeInteger(decrypt);
          //Hash the data
          MessageDigest md = MessageDigest.getInstance("SHA-256");
          md.update(data);
          byte[] digest = md.digest();
          //Create LargeInteger with hashed data
          LargeInteger result = new LargeInteger(digest);
          
          Path pubkey = Paths.get("pubkey.rsa");
           byte[]bytes = Files.readAllBytes(pubkey);
             
               byte[]n = new byte[64];
             for(int i = 0; i< n.length; i++)
             {
                 n[i] = bytes[i];
             }
             byte[]e = new byte[bytes.length-64];
             int j = 0;
             for(int i = n.length; i < bytes.length; i++)
             {
                 e[j] = bytes[i];
                 j++;
             }
          
         
          
          
          //Create E and N LargeInteger's with e and n byte arrays
          LargeInteger E = new LargeInteger(e);
          LargeInteger N = new LargeInteger(n);
         
          
          //Print E, N, signature, and result values for testing purposes
          System.out.println("E ");
          E.printArray();
          System.out.println("N "); 
          N.printArray();
          LargeInteger sig = decrypted.modularExp(E, N);
          System.out.println("sig");
          sig.printArray();
          System.out.println("result");
          result.printArray();            
           
          //Compare signature to result
          int answer = sig.compareTo(result);
          if(answer == 0)
          {
              System.out.println("Valid signature");
          }
          else
          {
              System.out.println("Invalid signature");
          }
        }
        catch(Exception e)
        {
            System.out.println("error reading from file");
            System.out.println(e.toString());
        }
    }
}


