import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.*;

class q1  {

    // returns the sum of 2 base-10 integers expressed as non-empty strings, perhaps with a leading "-".
    // e.g., add("0010","-9301") returns "-9291"
    // nb: returned string may have excess leading 0s.
    public static String add(String x,String y) {
        //System.out.println("adding " + x + " and " + y);
        String r = "";
        if (x.charAt(0)=='-') {
            if (y.charAt(0)=='-') {
                // -x + -y === - (x+y)
                r = '-' + add(x.substring(1),y.substring(1));
                return r;
            }
            // -x + y === y - x
            r = sub(y,x.substring(1));
            return r;
        } else if (y.charAt(0)=='-') {
            // x + -y === x - y
            r = sub(x,y.substring(1));
            return r;
        }

        // can assume both positive here

        // make sure same length
        int slen = x.length();
        if (y.length()!=slen) {
            slen = (y.length() > slen) ? y.length() : slen;
            x = pad(x,slen);
            y = pad(y,slen);
        }
        int carry = 0;
        for (int i=x.length()-1;i>=0;i--) {
            int sum = Character.getNumericValue(x.charAt(i))+Character.getNumericValue(y.charAt(i))+carry;
            if (sum>=10) {
                sum -= 10;
                carry = 1;
            } else {
                carry = 0;
            }
            r = sum + r;
        }
        if (carry!=0)
            r = "1" + r;
        return r;
    }

    // returns the difference between 2 base-10 integers expressed as non-empty strings, perhaps with a leading "-".
    // e.g., sub("0010","-9301") returns "9311"
    // nb: returned string may have excess leading 0s.
    static String sub(String x,String y) {
        String r = "";
        if (x.charAt(0)=='-') {
            if (y.charAt(0)=='-') {
                // -x - -y  === -x + y  === y - x
                r = sub(y.substring(1),x.substring(1));
                return r;
            }
            // -x - y === - (x+y)
            r = add(x.substring(1),y);
            if (r.length()>0 && r.charAt(0)!='-')
                r = "-" + r;
            return r;
        } else if (y.charAt(0)=='-') {
            // x - -y === x + y
            r = add(x,y.substring(1));
            return r;
        }

        int slen = x.length();
        if (y.length()!=slen) {
            slen = (y.length() > slen) ? y.length() : slen;
            x = pad(x,slen);
            y = pad(y,slen);
        }
        int borrow = 0;
        for (int i=x.length()-1;i>=0;i--) {
            int diff = Character.getNumericValue(x.charAt(i))-borrow-Character.getNumericValue(y.charAt(i));
            //System.out.println("sum of "+x.charAt(i)+"+"+y.charAt(i)+"+"+carry+" = "+sum);
            if (diff<0) {
                borrow = 1;
                diff += 10;
            } else {
                borrow = 0;
            }
            r = diff + r;
        }
        if (borrow!=0) { // flip it around and try again
            r = "-"+sub(y,x);
        }
        return r;
    }

    // remove unnecessary leading 0s from a base-10 number expressed as a string.
    public static String prune(String s) {
        if (s.charAt(0)=='-') return "-"+prune(s.substring(1));
        s = s.replaceFirst("^00*","");
        if (s.length()==0) return "0";
        return s;
    }

    // add leading 0s to a base-10 number expressed as a string to ensure the string
    // is of the length given.
    // nb: assumes a positive number input.
    public static String pad(String s,int n) {
        return String.format("%"+n+"s",s).replace(' ','0');
    }


    // just for testing add and sub
    public static void main(String[] args) {
        
        int n = Integer.valueOf(args[0]);
        String x = args[1];
        String y = args[2];

        if(n < 1){
            System.out.println("Please enter a value of n greater than 0!!");
            return;
        }
        

        ForkJoinPool forkJoinPool = new ForkJoinPool(n);
        long startTime = System.currentTimeMillis();
        String answer = forkJoinPool.invoke(new karatsuba(x,y));
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Karatsuba mulplication of "+ x +" and " + y +  " is " + answer + " and took " + totalTime);
        /* 
        n = 6;
        ForkJoinPool forkJoinPool2 = new ForkJoinPool(n);
        long startTime2 = System.currentTimeMillis();
        String answer2 = forkJoinPool2.invoke(new karatsuba(x,y));
        long endTime2 = System.currentTimeMillis();
        long totalTime2 = endTime2 - startTime2;
        System.out.println("Karatsuba mulplication of "+ x +" and " + y +  " is " + answer2 + " and took " + totalTime2);

        */
        /* 

        System.out.println(answer.equals(answer2));

        BigInteger x_big = new BigInteger(x);
        BigInteger y_big = new BigInteger(y);
        BigInteger answer3 = new BigInteger(answer2);

        BigInteger sum = x_big.multiply(y_big);

        System.out.println(sum.equals(answer3));

        System.out.println(totalTime + " " + totalTime2);

        */
    }

    // note: to test very large number inputs (on linux), invoke as follows
    // (replacing 10000 with as many digits as you want):
    // java q1 1 `tr -dc "[:digit:]" < /dev/urandom | head -c 10000` `tr -dc "[:digit:]" < /dev/urandom | head -c 10000`
}
