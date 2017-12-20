
import java.util.Random;
import java.math.BigInteger;
import java.lang.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;
public class LargeInteger {
    
    private final byte[] ONE = {(byte) 1};

    private byte[] val;

    /**
     * Construct the LargeInteger from a given byte array
     * @param b the byte array that this LargeInteger should represent
     */
    public LargeInteger(byte[] b) {
        val = b;
    }

    /**
     * Construct the LargeInteger by generatin a random n-bit number that is
     * probably prime (2^-100 chance of being composite).
     * @param n the bitlength of the requested integer
     * @param rnd instance of java.util.Random to use in prime generation
     */
    public LargeInteger(int n, Random rnd) {
        val = BigInteger.probablePrime(n, rnd).toByteArray();
    }
    
    /**
     * Return this LargeInteger's val
     * @return val
     */
    public byte[] getVal() {
        return val;
    }

    /**
     * Return the number of bytes in val
     * @return length of the val byte array
     */
    public int length() {
        return val.length;
    }

    /** 
     * Add a new byte as the most significant in this
     * @param extension the byte to place as most significant
     */
    public void extend(byte extension) {
        byte[] newv = new byte[val.length + 1];
        newv[0] = extension;
        for (int i = 0; i < val.length; i++) {
            newv[i + 1] = val[i];
            
        }
        val = newv;
    }

    /**
     * If this is negative, most significant bit will be 1 meaning most 
     * significant byte will be a negative signed number
     * @return true if this is negative, false if positive
     */
    public boolean isNegative() {
        return (val[0] < 0);
    }

    /**
     * Computes the sum of this and other
     * @param other the other LargeInteger to sum with this
     */
    public LargeInteger add(LargeInteger other) {
        byte[] a, b;
        // If operands are of different sizes, put larger first ...
        if (val.length < other.length()) {
            a = other.getVal();
            b = val;
        }
        else {
            a = val;
            b = other.getVal();
        }

        // ... and normalize size for convenience
        if (b.length < a.length) {
            int diff = a.length - b.length;

            byte pad = (byte) 0;
            if (b[0] < 0) {
                pad = (byte) 0xFF;
            }

            byte[] newb = new byte[a.length];
            for (int i = 0; i < diff; i++) {
                newb[i] = pad;
            }

            for (int i = 0; i < b.length; i++) {
                newb[i + diff] = b[i];
            }

            b = newb;
        }

        // Actually compute the add
        int carry = 0;
        byte[] res = new byte[a.length];
        for (int i = a.length - 1; i >= 0; i--) {
            // Be sure to bitmask so that cast of negative bytes does not
            //  introduce spurious 1 bits into result of cast
            carry = ((int) a[i] & 0xFF) + ((int) b[i] & 0xFF) + carry;

            // Assign to next byte
            res[i] = (byte) (carry & 0xFF);

            // Carry remainder over to next byte (always want to shift in 0s)
            carry = carry >>> 8;
        }

        LargeInteger res_li = new LargeInteger(res);
    
        // If both operands are positive, magnitude could increase as a result
        //  of addition
        if (!this.isNegative() && !other.isNegative()) {
            // If we have either a leftover carry value or we used the last
            //  bit in the most significant byte, we need to extend the result
            if (res_li.isNegative()) {
                res_li.extend((byte) carry);
            }
        }
        // Magnitude could also increase if both operands are negative
        else if (this.isNegative() && other.isNegative()) {
            if (!res_li.isNegative()) {
                res_li.extend((byte) 0xFF);
            }
        }

        // Note that result will always be the same size as biggest input
        //  (e.g., -127 + 128 will use 2 bytes to store the result value 1)
        return res_li;
    }

    /**
     * Negate val using two's complement representation
     * @return negation of this
     */
    public LargeInteger negate() {
        byte[] neg = new byte[val.length];
        int offset = 0;

        // Check to ensure we can represent negation in same length
        //  (e.g., -128 can be represented in 8 bits using two's 
        //  complement, +128 requires 9)
        if (val[0] == (byte) 0x80) { // 0x80 is 10000000
            boolean needs_ex = true;
            for (int i = 1; i < val.length; i++) {
                if (val[i] != (byte) 0) {
                    needs_ex = false;
                    break;
                }
            }
            // if first byte is 0x80 and all others are 0, must extend
            if (needs_ex) {
                neg = new byte[val.length + 1];
                neg[0] = (byte) 0;
                offset = 1;
            }
        }

        // flip all bits
        for (int i  = 0; i < val.length; i++) {
            neg[i + offset] = (byte) ~val[i];
        }

        LargeInteger neg_li = new LargeInteger(neg);
    
        // add 1 to complete two's complement negation
        return neg_li.add(new LargeInteger(ONE));
    }

    /**
     * Implement subtraction as simply negation and addition
     * @param other LargeInteger to subtract from this
     * @return difference of this and other
     */
    public LargeInteger subtract(LargeInteger other) {
        return this.add(other.negate());
    }

    public int compareTo(LargeInteger other)
    {
        if(this.equals(other))
        {
            return 0;
        }
        else if(this.subtract(other).isNegative())
        {
            return -1;
        }
        return 1;
    }
    
    public boolean equals(LargeInteger other)
    {

        byte[] array = other.getVal();

        for(int i = 0; i < this.length(); i++)
        {
            if(this.getVal()[i] != array[i])
            {
                return false;
            }
        }
        return true;
    }
    
    public LargeInteger lShift()//int amt)
    {

        byte[] array = this.getVal();
        byte[] shifted = new byte[array.length];
        byte PrevMSB = (byte)msb((byte)array[array.length-1]);
        byte NewMSB = 0;
 
        shifted[array.length-1] = (byte)(array[array.length-1] << 1);
        for(int i = array.length-2; i >= 0; i--)
        {
   
           NewMSB = (byte)msb((byte)array[i]);
            shifted[i] = (byte)(((byte)(array[i] << 1)) | (byte)PrevMSB);
            PrevMSB = NewMSB;
        }
        
        if(PrevMSB == 1)
        {
            byte x = 0x01;
            byte[] shiftedCopy = new byte[shifted.length];
            for(int i = 0; i < shiftedCopy.length; i++)
            {
              shiftedCopy[i] = shifted[i];
            }
            shifted = new byte[shifted.length+1];
            shifted[0] = x;
            for(int i = 1; i < shifted.length; i++)
            {
                shifted[i] = shiftedCopy[i-1];
            }
        }
        LargeInteger result = new LargeInteger(shifted);

        return result;
    }
    
     public LargeInteger rShift()//int amt)
    {
        byte[] array = this.getVal();
        byte[] shifted = new byte[array.length];
        byte PrevLSB = (byte)lsb((byte)array[0]);
        byte NewLSB = 0;
        shifted[0] = (byte)(array[0] >>> 1);
        for(int i = 1; i <= array.length-1; i++)
        {

           NewLSB = (byte)lsb((byte)array[i]);
       
           shifted[i] = (byte)(((byte)(array[i] >>> 1)&0x7F) | (PrevLSB << 7));
      
           PrevLSB = NewLSB;
        }

        LargeInteger result = new LargeInteger(shifted);
        return result;
    }
    
    public LargeInteger copy()
    {
        byte[] array = this.getVal();
        byte[] copy = new byte[this.getVal().length];
        for(int i = 0; i < array.length; i++)
        {
            copy[i] = array[i];
        }
        return new LargeInteger(copy);
    }

    
    //while(multiplier != 0)
    
    /**
     * Compute the product of this and other
     * @param other LargeInteger to multiply by this
     * @return product of this and other
     */
    public LargeInteger multiply(LargeInteger other) {
        // YOUR CODE HERE (replace the return, too...)
        LargeInteger multiplier = other.copy();
        LargeInteger multiplicand = this.copy();
     
        boolean negative = false;
        if(multiplier.isNegative() && multiplicand.isNegative())
        {
            negative = false;
            multiplier = multiplier.negate();
    
            multiplicand = multiplicand.negate();
           
        }
        else if(multiplier.isNegative())
        {
             negative = true;
             multiplier = multiplier.negate();
               
        }
        else if(multiplicand.isNegative())
        {
            negative = true;
            multiplicand = multiplicand.negate();
            
            
        }
        int length = 0;
        if(multiplier.getVal().length > multiplicand.getVal().length)
        {
            length = multiplier.getVal().length;
        }
        else
        {
            length = multiplicand.getVal().length;
        }
        byte[] product = new byte[length * 2];
        
        LargeInteger p = new LargeInteger(product);
        byte [] z = new byte[multiplier.getVal().length];
        LargeInteger zero = new LargeInteger(z);
       
        while(multiplier.compareTo(zero) != 0)
        {
        if((multiplier.getLSB() != 0) )
        {
            
           p = p.add(multiplicand);
           
        }
           multiplier =  multiplier.rShift();

           multiplicand =  multiplicand.lShift();
      
    }
 
        if(negative == true)
        {
           p = p.negate();
        }

        return p;
    }
    
    public void printArray()
    {
        byte[] array = this.getVal();
        String binary = "";
        for(int i = 0; i < array.length; i++)
        {
            binary += Integer.toBinaryString(array[i] &0xFF) + " ";
        }
        System.out.println(binary);
    }
    
   
    
    /**
     * Run the extended Euclidean algorithm on this and other
     * @param other another LargeInteger
     * @return an array structured as follows:
     *   0:  the GCD of this and other
     *   1:  a valid x value
     *   2:  a valid y value
     * such that this * x + other * y == GCD in index 0
     */
    
     public LargeInteger[] XGCD(LargeInteger other) {
        // YOUR CODE HERE (replace the return, too...)
        //have to do division and modulous (keep quotient and a remainder)
        LargeInteger th = this.copy();
        LargeInteger oth = other.copy();
     
        LargeInteger div = th.divide(oth);
        LargeInteger mod = th.mod(oth);
        
        byte [] z = new byte[oth.length()];
        LargeInteger zero = new LargeInteger(z);
        byte[] dst = new byte[2];
        
        
        
      
        
       if(other.compareTo(zero)!=0)
       {
         LargeInteger[] dstArray = other.XGCD(mod); 
         LargeInteger s = dstArray[1];
         LargeInteger t = dstArray[2];
         System.out.println("a/b ");
         div.printArray();
         t = s.subtract(div.multiply(t));
         dstArray[1] = dstArray[2];
         dstArray[2] = t;
         System.out.println(" s ");
         dstArray[1].printArray();
         System.out.println(" t ");
         dstArray[2].printArray();
         return dstArray;
       }
            
       byte[] one = {(byte) 0x01};
       LargeInteger o = new LargeInteger(one);
       LargeInteger[] dstArray = {this, o, zero};
        System.out.println(" s ");
         dstArray[1].printArray();
         System.out.println(" t ");
         dstArray[2].printArray();
       return dstArray;
        
     }
     
     
     public LargeInteger[] division(LargeInteger other)//change return
     {
         LargeInteger divisor = other.copy();
         LargeInteger dividend = this.copy();
          boolean negative = false;
        if(divisor.isNegative() && dividend.isNegative())
        {
            negative = false;
            divisor = divisor.negate();
            
            dividend = dividend.negate();
            
        }
        else if(divisor.isNegative())
        {
             negative = true;
             divisor = divisor.negate();
              
        }
        else if(dividend.isNegative())
        {
            negative = true;
            dividend = dividend.negate();
            
           
        }
         
        byte []add = {(byte)0x00};
        int length = 0;
        if(divisor.getVal().length > dividend.getVal().length)
        {
            length = divisor.getVal().length;
        }
        else
        {
            length = dividend.getVal().length;
        }
        byte[] a = new byte[length];
        LargeInteger remainder = new LargeInteger(a);
        LargeInteger quotient = new LargeInteger(a);
        byte [] z = new byte[dividend.length()];
        LargeInteger zero = new LargeInteger(z);
        LargeInteger addVal = new LargeInteger(add);
        int dMSB = 0;
        int qMSB = 0;
        for(int i = 0; i< dividend.length() * 8; i++)
         {
            dMSB =  dividend.getMSB();
            qMSB = quotient.getMSB();
            remainder = remainder.leftShift();
            remainder.getVal()[remainder.length()-1] = (byte)((byte)remainder.getVal()[remainder.length()-1] | ((byte)dMSB));
            dividend = dividend.leftShift();
            dividend.getVal()[dividend.length()-1] = (byte)((byte)dividend.getVal()[dividend.length()-1] | ((byte)qMSB));
            quotient = quotient.leftShift();
            
             if(divisor.compareTo(remainder) == -1 || divisor.compareTo(remainder) == 0 )
            {
                remainder = remainder.subtract(divisor);
                //subtract(divisor); //figure out how to use subtract
                add[0] = 0x01;
            }
            else
            {
                add[0] = 0x00;
            }
            quotient = quotient.add(addVal);
        }
    
         LargeInteger product;
         if(negative == true)
         {
             quotient = quotient.negate();
             product = quotient.multiply(other);
             remainder = this.subtract(product).add(other);
         }
         
         LargeInteger[] answers = new LargeInteger[2];
         answers[0] = quotient;
         answers[1] = remainder;
         return answers;
     }
     
     public LargeInteger divide(LargeInteger other)
     {
        LargeInteger[] answers = this.division(other);
        return answers[0];
     }
     
     public LargeInteger mod(LargeInteger other)
     {
        LargeInteger[] answers = this.division(other);
         return answers[1];
         
     }
     
     public boolean lessThan(LargeInteger i)
     {
       if (subtract(i).isNegative() )
       {
           return true;
        }
         return false;
     }
     
     public LargeInteger leftShift()//int amt)
    {
        byte[] array = this.getVal();
        byte[] shifted = new byte[array.length];
        byte PrevMSB = (byte)msb((byte)array[array.length-1]);
        byte NewMSB = 0;
        shifted[array.length-1] = (byte)(array[array.length-1] << 1);
        for(int i = array.length-2; i >= 0; i--)
        { 
           NewMSB = (byte)msb((byte)array[i]);
            shifted[i] = (byte)(((byte)(array[i] << 1)) | (byte)PrevMSB);
            PrevMSB = NewMSB;
        }
        LargeInteger result = new LargeInteger(shifted);
        return result;
    }
     
     private int msb(byte x)
     {
         byte bitmask = (byte) 0x01;
         byte y = (byte)((x >>> 7)&0x7F);
         int m = (int)(y & bitmask);
         return m;
     }
     
     private int lsb(byte x)
     { 
         byte bitmask = (byte) 0x01;
         int m = (int)(x & bitmask);
         return m;
     }
     
     public int getMSB()
     {
         byte[]array = this.getVal();
         byte x = array[0];
         byte bitmask = (byte) 0x01;
         byte y = (byte)((x >>> 7)&0x7F);
         int m = (int)(y & bitmask);
         return m;
     }
     
     public int getLSB()
     {
         byte[]array = this.getVal();
         byte x = array[this.getVal().length-1];
         byte bitmask = (byte) 0x01;
         int m = (int)(x & bitmask);
         return m;
     }
     
     public byte and(byte shift, int lsb)
     {
        byte bitmask = (byte) 0xFE; 
        byte a = (byte) (((byte) lsb) | 0xFE);
        byte b = (byte) (shift & a);
        return b;
     }

     /**
      * Compute the result of raising this to the power of y mod n
      * @param y exponent to raise this to
      * @param n modulus value to use
      * @return this^y mod n
      */
     public LargeInteger modularExp(LargeInteger y, LargeInteger n) {
   
        LargeInteger value = y.mod(n);
        byte[] newV;
        if(value.length() < 4)
        {
        byte[] copy = new byte[value.getVal().length];
       for(int i = 0; i < copy.length; i++)
       {
          copy[i] = value.getVal()[i];
       }
        newV = new byte[4];
       int q = copy.length-1;
  
       for(int i = newV.length-1; i > 0; i--)
       {
           
           if(q >= 0)
           {
           newV[i] = copy[q];
            q--;
        }
           
          
        }
        
       }
      else
       {
          newV = value.getVal();
       }
        ByteBuffer wrapped = ByteBuffer.wrap(newV);
     
      
        
        
        int v = wrapped.getInt();
        LargeInteger mult = new LargeInteger(this.getVal());
        for(int i = 0; i < v-1; i++)
        {
            mult = mult.multiply(mult);
        }
        return mult;
     }
}
