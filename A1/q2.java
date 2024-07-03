import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

public class q2 {

    // an input image
    public static BufferedImage imgIn;

    // output image
    public static BufferedImage imgOut;

    // parameters and their default values
    public static String imagebase = "cat" ; // base name of the input images; actual names append "1.png", "2.png" etc.
    public static int threads = 6; // number of threads to use
    public static int outputheight = 4096; // output image height
    public static int outputwidth = 4096; // output image width
    public static int attempts = 2000; // number of failed attempts before a thread gives up

    public static boolean[][] verificationImage = new boolean[outputheight][outputwidth];

    public static long[] executionTimes = new long[100];

    // print out command-line parameter help and exit
    public static void help(String s) {
        System.out.println("Could not parse argument \""+s+"\".  Please use only the following arguments:");
        System.out.println(" -i inputimagebasename (string; current=\""+imagebase+"\")");
        System.out.println(" -h outputimageheight (integer; current=\""+outputheight+"\")");
        System.out.println(" -w outputimagewidth (integer; current=\""+outputwidth+"\")");
        System.out.println(" -a attempts (integer value >=1; current=\""+attempts+"\")");
        System.out.println(" -t threads (integer value >=1; current=\""+threads+"\")");
        System.exit(1);
    }

    // process command-line options
    public static void opts(String[] args) {
        int i = 0;

        try {
            for (;i<args.length;i++) {

                if (i==args.length-1)
                    help(args[i]);

                if (args[i].equals("-i")) {
                    imagebase = args[i+1];
                } else if (args[i].equals("-h")) {
                    outputheight = Integer.parseInt(args[i+1]);
                    verificationImage = new boolean[outputheight][outputwidth];
                } else if (args[i].equals("-w")) {
                    outputwidth = Integer.parseInt(args[i+1]);
                    verificationImage = new boolean[outputheight][outputwidth];
                } else if (args[i].equals("-t")) {
                    threads = Integer.parseInt(args[i+1]);
                } else if (args[i].equals("-a")) {
                    attempts = Integer.parseInt(args[i+1]);
                } else {
                    help(args[i]);
                }
                // an extra increment since our options consist of 2 pieces
                i++;
            }
        } catch (Exception e) {
            System.err.println(e);
            help(args[i]);
        }
    }

    // main.  we allow an IOException in case the image loading/storing fails.
    public static void main(String[] args) throws IOException {
        // run the program 101 times discarding the first and averaging the rest
        for(int j = 100; j >= 0; j-- ){
            // process options
            opts(args);
            verificationImage = new boolean[outputheight][outputwidth];

            // create an output image
            imgOut = new BufferedImage(outputwidth,outputheight,BufferedImage.TYPE_INT_ARGB);

            // read in the input images (icons) for each thread and initialize the painters. 
            // 1 painter for each thread 
            painter[] painters = new painter[threads];
            for (int i = 0; i < threads; i++) {
                imgIn = ImageIO.read(new File(imagebase+ String.valueOf(i+1) + ".png"));
                int imwidth  = imgIn.getWidth();
                int imheight = imgIn.getHeight();

                painters[i] = new painter(imgOut, imgIn, imwidth, imheight, outputwidth,outputheight, verificationImage, attempts);
            }

            
            //create the threads
            Thread[] thread = new Thread[threads];
            for(int i = 0; i < painters.length; i++) {
                thread[i] = new Thread(painters[i]);
            }
            

            long timeStart = System.currentTimeMillis();
            //run the threads
            for (int i = 0; i < thread.length; i++) {
                thread[i].start();
            }

            for (int i = 0; i < thread.length; i++) {
                try {
                    thread[i].join();
                    
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            long timeEnd = System.currentTimeMillis();

            long totalTime = timeEnd - timeStart;
            if(j == 100){

            }else{
                executionTimes[j] = totalTime;
            }
            // Write out the image   
            File outputfile = new File( "outputimage.png");
            ImageIO.write(imgOut, "png", outputfile);
        }
        // add up the times and average them
        long average = 0;
        for(int k = 0; k < executionTimes.length; k++){
            average = average + executionTimes[k];
        }
        
        System.out.println(average/100.000);
    }
}
