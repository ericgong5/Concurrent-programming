import java.lang.Math;  



public class station implements Runnable{


    public boolean stationIsOpen = true; //flag to indicate if metro is closed
    public int localCount = 0; // local count of users

    @Override
    public void run() {
        while(true){

            //sleep 10 ms
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //lock individual station when incrementing/decrementing local count
            synchronized(this){
                if(stationIsOpen && Math.random() < 0.5100){
                    this.localCount++;
                }else{
                    this.localCount--;
                }
            }
        }
    }  
}
