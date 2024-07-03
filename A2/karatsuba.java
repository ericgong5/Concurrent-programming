import java.util.*;
import java.util.concurrent.*;

public class karatsuba extends RecursiveTask<String>{

    private String x;
    private String y;

    karatsuba(String x, String y) {
        super();
        this.x = x;
        this.y = y;
    }

    @Override
    protected String compute() {
        //strip the leading '-' sign if there
        String sign = "";
        if (x.charAt(0) == '-') {
            if (y.charAt(0) == '-') {
                // -x * -y ===  x * y
                x = x.substring(1);
                y = y.substring(1);
            }else{
                // -x * y  === - (y * x)
                sign = "-";
                x = x.substring(1);
            }
        } else if (y.charAt(0) == '-') {
                // x * -y === -  (x * y)
                sign = "-";
                y = y.substring(1);
        }
        //get rid of leading 0s
        x = q1.prune(x);
        y = q1.prune(y);

        // base case where both strings are of size 1
        if (x.length() == 1 && y.length() == 1){
            int base = Character.getNumericValue(y.charAt(0)) * Character.getNumericValue(x.charAt(0));
            return sign + Integer.toString(base);
        } 

        // get i (power of 10) 
        int maxLength = Math.max(x.length(),y.length());
        int power_of_10 = (int) Math.ceil(maxLength / 2.0);
        x = q1.pad(x,maxLength);
        y = q1.pad(y,maxLength);

        String xH,yH,xL,yL;
        // get components a = xHyH, b = xLyL, c = (xH + xL)(yH + yL) − a − b
        int cutoff = maxLength - power_of_10;
        xH = x.substring(0,cutoff);
        yH = y.substring(0,cutoff);
        xL = x.substring(cutoff);
        yL = y.substring(cutoff);
        
        // have the sums of the multiplication of C
        String x_Sum = q1.add(xH, xL);
        String y_Sum = q1.add(yH, yL);
        
        // fork off a, b and c
        RecursiveTask<String> a = new karatsuba(xH,yH);
        RecursiveTask<String> b = new karatsuba(xL,yL);
        RecursiveTask<String> incomplete_c = new karatsuba(x_Sum,y_Sum);
        a.fork();
        b.fork();
        incomplete_c.fork();

        // join to get results
        String A = a.join();
        String B = b.join();
        String C = incomplete_c.join();
        // calculate C
        C = q1.sub(C, A);
        C = q1.sub(C, B);
        // pad the numbers 
        A = A + q1.pad("0", power_of_10*2);
        C = C + q1.pad("0", power_of_10);

        String result = "";

        result = q1.add(A,C);
        result = q1.add(result, B);

        // make sure sign is correct
        if(sign == "-" && result.charAt(0) == '-'){
            return q1.prune(result.substring(1));
        }else if(sign == "-"){
            return "-" + q1.prune(result);
        }else if(result.charAt(0) == '-'){
            return "-" + q1.prune(result.substring(1));
        }else{
            return q1.prune(result);
        }
    }

    public static void main(String[] args) {
    
    }
    
}
