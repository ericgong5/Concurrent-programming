public class overseer implements Runnable{

    public station station1 = new station();
    public station station2 = new station();
    public station station3 = new station();
    public station station4 = new station();   //each station
    public int q; // time in ms between checks
    public int n; // max users in metro
    public int globalCount = 0; //global count
    public boolean metroIsClosed = false; // flag to indicate max users reached

    // initialize the overseer
    public overseer(station[] stations, int n, int q){
        this.station1 = stations[0];
        this.station2 = stations[1];
        this.station3 = stations[2];
        this.station4 = stations[3];
        this.q = q;
        this.n = n;
    }

    //check of the global count to determine if we close the metro
    boolean checkGlobalCount(int target){
        this.globalCount = 0;

        //start locking each station
        synchronized(this.station1){
            globalCount += this.station1.localCount;
            System.out.println("Station 1 has " + this.station1.localCount + " users currently.");

            synchronized(this.station2){
                globalCount += this.station2.localCount;
                System.out.println("Station 2 has " + this.station2.localCount + " users currently.");

                synchronized(this.station3){
                    globalCount += this.station3.localCount;
                    System.out.println("Station 3 has " + this.station3.localCount + " users currently.");

                    synchronized(this.station4){
                        globalCount += this.station4.localCount;
                        System.out.println("Station 4 has " + this.station4.localCount + " users currently.");
                        System.out.println("The Metro system has a total of " + globalCount + " users currently.");

                        //once every station is locked, compare the counts
                        if(globalCount >= target){
                            metroIsClosed = true;
                        }else{
                            metroIsClosed = false;
                        }

                        //tell the stations to close if neccessary
                        if(metroIsClosed){
                            this.station4.stationIsOpen = false;
                        }else{
                            this.station4.stationIsOpen = true;
                        }
                    }

                    if(metroIsClosed){
                        this.station3.stationIsOpen = false;
                    }else{
                        this.station3.stationIsOpen = true;
                    }
                }

                if(metroIsClosed){
                    this.station2.stationIsOpen = false;
                }else{
                    this.station2.stationIsOpen = true;
                }
            }

            if(metroIsClosed){
                this.station1.stationIsOpen = false;
            }else{
                this.station1.stationIsOpen = true;
            }
        }
        return metroIsClosed;
    }


    @Override
    public void run() {
        while(true){
            //wait for q ms
            try {
                Thread.sleep(q);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            //check teh global count and enter if we hit n
            if(checkGlobalCount(n)){

                while(true){
                    //wait for q/10 ms
                    try {
                        Thread.sleep(q/10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //calculate the target user count for re-opening the metro
                    int target = (int) Math.floor(0.75 * (double) n);
                    if(!checkGlobalCount(target)){
                        break;
                    }
                }
            }
        }
    }
}
