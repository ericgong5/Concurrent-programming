import java.awt.image.*;


public class painter implements Runnable{
    
    public BufferedImage iconToCopy; //the icon we are copying for each thread
    public BufferedImage outputImage; // the output image
    public boolean[][] verificationImage; // a shared verification matrix for overlap checking
    public int width;
    public int height;
    public int attemptsLeft;
    public int maxHeight; // height of output image
    public int maxWidth; // width of output image



    public painter(BufferedImage outputImage, BufferedImage iconToCopy, int width, int height,int maxWidth, int maxHeight, boolean[][] verificationImage, int attemptsLeft){
        this.outputImage = outputImage;
        this.iconToCopy = iconToCopy;
        this.width = width;
        this.height = height;
        this.verificationImage = verificationImage;
        this.attemptsLeft = attemptsLeft;
        this.maxHeight = maxHeight;
        this.maxWidth = maxWidth;
    }

    @Override
    public void run() {
        while(attemptsLeft != 0){
            //randomly generate a new position that is within bounds
            int newStartingIPosition = (int) (Math.random() * (this.maxHeight-this.height + 1));
            int newStartingJPosition = (int) (Math.random() * (this.maxWidth-this.width + 1));
            
            boolean isOverlapping = false;

            // start verifying for overlap + updating of the verification matrix
            synchronized(this.verificationImage){
                //check if we have overlapping icons
                for (int i = newStartingIPosition; i < newStartingIPosition + this.height; i++) {
                    for (int j = newStartingJPosition; j < newStartingJPosition + this.width; j++) {
                        if(verificationImage[i][j] == true){
                            isOverlapping = true;
                            break;
                        }
                    }
                    if(isOverlapping){
                        break;
                    }
                }
                //if we dont fill in the verification image for next thread to compare with
                if(!isOverlapping){
                    for (int i = newStartingIPosition; i < newStartingIPosition + this.height; i++) {
                        for (int j = newStartingJPosition; j < newStartingJPosition + this.width; j++) {
                            verificationImage[i][j] = true;   
                        }
                    }
                }
            }
            // we found overlap and derement the attempts
            if(isOverlapping){
                attemptsLeft--;
            }else{
                //put icon in the output image
                for (int i = 0; i < this.height; i++) {
                    for (int j = 0; j < this.width; j++) {
                        outputImage.setRGB(newStartingJPosition + j, newStartingIPosition + i,iconToCopy.getRGB(j,i));
                    }
                }
            }
        }   
    }
    
}
