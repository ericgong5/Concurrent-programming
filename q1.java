

public class q1 {

    
    

    public static void main(String[] args) {

        //check the input first
        checkParameters(args[0], args[1]);
        final int n = Integer.parseInt(args[0]);
        final int q = Integer.parseInt(args[1]);
        
        //create the 4 stations
        station[] stations = new station[4];
        for(int i = 0; i < 4; i++){
            stations[i] = new station();
        }
        //create the overseers
        overseer overseer = new overseer(stations, n, q);

        //create the threads
        Thread[] thread = new Thread[5];
        for(int i = 0; i < stations.length; i++) {
            thread[i] = new Thread(stations[i]);
        }
        thread[4] = new Thread(overseer);
        
        //run the threads
        for (int i = 0; i < thread.length; i++) {
            thread[i].start();
        }
        
        
    }









    // input validation for n and q
    static void checkParameters(String firstParameter, String secondParameter){
        int n = 0;
        int q = 0;
        
        n = Integer.parseInt(firstParameter);
        q = Integer.parseInt(secondParameter);
     
        if(n < 5){
            throw new IllegalArgumentException("n must be equal to or greater than 10!!");
        }
        if(q < 10 || q%10 != 0){
            throw new IllegalArgumentException("q must be evenly divisible by 10 and equal to or greater than 10!!");
        }

    }
 
}

