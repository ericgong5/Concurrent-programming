#include <omp.h>
#include <stdlib.h>
#include <stdio.h>
#include <sys/time.h>

// helper function for generating a string
char* create_string(int n){
        char* string = malloc (sizeof (char) * n+1);
        static const char characters[22] = {'0','0','1','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f','x','x','y'};
        srand(time(NULL));
        for(int i = 0; i < n ;i++){
            char rdm = characters[rand()%21];
            string[i] = rdm;
        }
        string[n] = '\0';
        return string;
}

// helper function to get time in ms
long long timeInMilliseconds(void) {
    struct timeval time;
    gettimeofday(&time,NULL);
    return (((long long)time.tv_sec)*1000)+(time.tv_usec/1000);
}

/* To compile this (on linux):
 *   gcc -o q2 -fopenmp q2.c
 */
int main(int argc,char *argv[]) {    
    int t = atoi(argv[1]);   // number of optimistic threads
    int n = atoi(argv[2]);   // size of string
    int num_threads = t+1;

    omp_set_dynamic(0); /* Disable dynamic teams. */
    omp_set_num_threads(num_threads);

    // create our random string (should be read only from here on)
    char* string = create_string(n);  
    printf("Random Input string is: %s\n", string);   // made a helper in main to print string to a file instead of crowding screen

    // start timer
    long long start_time = timeInMilliseconds();
    
    int segment_size = n;
    //array of all the indexes where we divide the string into t threads (should be read only)
    int segment_index[num_threads*2];
    segment_index[0] = 0;
    segment_index[1] = n-1;
    // since each optimistic thread has to do 4 passes, thread 0 should be ~4 times bigger than the others to balance workload
    if (num_threads != 1){
        segment_size = n / (num_threads + 3);  // N = (t-1)x + 4x   -> N / (t+3) 
        int extra = n - (segment_size * (num_threads+3)); // add the leftover characters on the first thread
        segment_index[1] = (segment_size*4) + extra - 1; //add the end index of the first segment
    }

    //add all the other segment indexes
    for(int i = 2; i<num_threads*2;i=i+2){
        segment_index[i] = segment_index[i-1] + 1;
        segment_index[i+1] = segment_index[i-1] + segment_size;
    }
    
    // array to store results of mapping of each threads endstate/counts
    // we get a 2 endstate,count tuple for each possible starting state (4 in this dfa) + 2 for the naive thread
    // which totals 2 + (num_threads-1)*4*2
    // order is always endstate then count with state 0,1,2 then 3 
    // WRITE ONLY                     (.....actually read and write after the parrallel part)
    int segment_results[(num_threads-1)*4*2 + 2]; 
    int thread_id;
    #pragma omp parallel private(thread_id) shared(segment_index,segment_results,string)
    {
        thread_id = omp_get_thread_num();
        int start = segment_index[thread_id*2];       //get start and end index of the string segment for its thread number
        int end = segment_index[thread_id*2+1] + 1;
        // naive thread, keeps count and goes through dfa then write resulting end state and count
        if(thread_id == 0){
            int current_state = 0; 
            int count = 0;
            for(int i=0;i<end;i++){
                char current_char = string[i];
                if(current_state == 0){
                    if(current_char >= '1' && current_char <= '9'){
                        current_state = 1;
                    }else{
                        current_state = 0;
                    }
                }else if(current_state == 1){
                    if((current_char >= '1' && current_char <= '9') || (current_char >= 'a' && current_char <= 'f')){
                        current_state = 2;
                    }else{
                        current_state = 0;
                    }
                }else if(current_state == 2){
                    if((current_char >= '0' && current_char <= '9') || (current_char >= 'a' && current_char <= 'f')){
                        current_state = 3;
                    }else{
                        current_state = 0;
                    }
                }else if(current_state == 3){
                    if((current_char >= '0' && current_char <= '9') || (current_char >= 'a' && current_char <= 'f')){
                        current_state = 3;
                    }else{
                        current_state = 0;
                        count++;
                    }
                }
            }
            #pragma omp critical
            {
            // first 2 entries in front from thread 0 
            segment_results[thread_id] = current_state;
            segment_results[thread_id+1] = count;
            }

        // optimistic threads, keeps count and goes through dfa then write resulting end state and count
        }else if(thread_id != 0 && num_threads > 1){    
            #pragma omp parallel for 
                for(int j=0;j<4;j++){
                    int current_state = j;
                    int count = 0;
                    for(int i=start;i<end;i++){
                        char current_char = string[i];
                        if(current_state == 0){
                            if(current_char >= '1' && current_char <= '9'){
                                current_state = 1;
                            }else{
                                current_state = 0;
                            }
                        }else if(current_state == 1){
                            if((current_char >= '1' && current_char <= '9') || (current_char >= 'a' && current_char <= 'f')){
                                current_state = 2;
                            }else{
                                current_state = 0;
                            }
                        }else if(current_state == 2){
                            if((current_char >= '0' && current_char <= '9') || (current_char >= 'a' && current_char <= 'f')){
                                current_state = 3;
                            }else{
                                current_state = 0;
                            }
                        }else if(current_state == 3){
                            if((current_char >= '0' && current_char <= '9') || (current_char >= 'a' && current_char <= 'f')){
                                current_state = 3;
                            }else{
                                current_state = 0;
                                count++;
                            }
                        }
                    }
                    #pragma omp critical
                    {
                    // we have 2 entries in front from thread 0 and from then on we jump by 8 for each thread
                    // j is the starting state so we increment by 2 for each state,count tuple
                    segment_results[2 + (thread_id-1)*8 + j*2] = current_state;
                    segment_results[2 + (thread_id-1)*8 + j*2 + 1] = count;
                    }       
                } 
        } 
        
    }

    // take in the endstate and count of thread 0
    int end_state = segment_results[0];
    int total_count = segment_results[1];
    int index = 2 + end_state*2;
    // go through thread by thread in order to get proper count
    for(int i=1;i<t+1;i++){
        end_state = segment_results[index];
        total_count = total_count + segment_results[index+1];
        index = 2 + i * 8 + end_state*2;
    }
    if(end_state == 3){
        total_count = total_count + 1;
    }

    //stop timer
    long long end_time = timeInMilliseconds();

    printf("Matching found %d matches and took %lu ms.\n", total_count, end_time-start_time);


    // helper to print input string to a txt file instead of crowding the screen
    /*
    FILE *fptr;
    fptr = fopen("input_string.txt", "w");
    fprintf(fptr, string);
    fclose(fptr);
    */

    free(string);
}