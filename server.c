#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <pthread.h>
#include <sys/types.h>
#include <signal.h>

#define MAX_CLIENTS 100
#define BUFFER_SZ 2048

static _Atomic unsigned int cli_count = 0;
static int uid = 10;

/* Client structure */
typedef struct{
	struct sockaddr_in address;
	int sockfd;
	int uid;
	char name[32];
} client_t;

client_t *clients[MAX_CLIENTS];

pthread_mutex_t clients_mutex = PTHREAD_MUTEX_INITIALIZER;

void str_overwrite_stdout() {
    printf("\r%s", "> ");
    fflush(stdout);
}

void str_trim_lf (char* arr, int length) {
  int i;
  for (i = 0; i < length; i++) { // trim \n
    if (arr[i] == '\n') {
      arr[i] = '\0';
      break;
    }
  }
}

int exists(const char *fname)
{
    FILE *file;
    if ((file = fopen(fname, "r")))
    {
        fclose(file);
        return 1;
    }
    return 0;
}

void print_client_addr(struct sockaddr_in addr){
    printf("%d.%d.%d.%d",
        addr.sin_addr.s_addr & 0xff,
        (addr.sin_addr.s_addr & 0xff00) >> 8,
        (addr.sin_addr.s_addr & 0xff0000) >> 16,
        (addr.sin_addr.s_addr & 0xff000000) >> 24);
}

/* Add clients to queue */
void queue_add(client_t *cl){
	

	for(int i=0; i < MAX_CLIENTS; ++i){
		if(!clients[i]){
			clients[i] = cl;
			break;
		}
	}

	
}

void queue_init(){
	
	for(int i=0; i < MAX_CLIENTS; ++i){
		if(clients[i]){
			clients[i] = NULL;
		}
	}
	
}

/* Remove clients to queue */
void queue_remove(int uid){
	

	for(int i=0; i < MAX_CLIENTS; ++i){
		if(clients[i]){
			if(clients[i]->uid==uid){
				clients[i] = NULL;
				break;
			}
		}
	}

	
}


/* Send message */
void send_message(char *s, char *cname){
	pthread_mutex_lock(&clients_mutex);
	for(int i=0; i<MAX_CLIENTS; ++i){
		if(clients[i]){
			if(!strcmp(clients[i]->name,cname)){
				if(write(clients[i]->sockfd, s, strlen(s)) < 0){
					perror("ERROR: write to descriptor failed");
					break;
				}
			}
		}
	}
	pthread_mutex_unlock(&clients_mutex);
}

/* Handle all communication with the client */
void *handle_client(void *arg){
	char buff_out[BUFFER_SZ];
	char name[32];
	char password[32];
	char last_friend[32];
	char friend[32];
	char number[32];
	char correct_pass[32];
	char filename[64] = "";
	char friend_filename[64] = "";
	char temp_filename[64] = "exit.txt";
	char txt[]=".txt";
	int exist_flag = 0;
	int leave_flag = 0;

	cli_count++;
	client_t *cli = (client_t *)arg;
	FILE *cf; //client file
	FILE *temp_cf; //temporary file for deleting friends

	//Log in/Sign up
	while(1){
		if(recv(cli->sockfd, number, 32, 0) <= 0){
			printf("Didn't enter the number.\n");
		} else{
			if(!strcmp(number,"1")){// LOG IN
				while(1){
					
					memset(cli->name, 0, sizeof(cli->name));
					recv(cli->sockfd, name, 32, 0);
					memset(filename, 0, sizeof(filename));
					strcat(cli->name,name);
					strcat(filename,cli->name);
					strcat(filename,txt);
					if(exists(filename)==0){
						send_message("false", cli->name);
					}
					else{
						send_message("truee", cli->name);
						
						break;
					}
				}
				cf = fopen(filename,"r");
				fgets(name,32,cf);
				fgets(correct_pass,32,cf);
				
				str_trim_lf(correct_pass, strlen(correct_pass));
				while(1){
					if(recv(cli->sockfd, password, 32, 0) <= 0){
						printf("Didn't enter the password.\n");
						leave_flag = 1;
					}
					else{
						str_trim_lf(password, strlen(password));
						if(strcmp(correct_pass,password)){
							send_message("false", cli->name);
						}
						else{
							send_message("truee", cli->name);
							
							break;
						}
					}
					
				}
				break;
			}
			else if(!strcmp(number,"2")){ //SIGN UP
				while(1){
					
					memset(cli->name, 0, sizeof(cli->name));
					recv(cli->sockfd, name, 32, 0);
					memset(filename, 0, sizeof(filename));
					strcat(cli->name,name);
					strcat(filename,cli->name);
					strcat(filename,txt);
					if(exists(filename)==1){
						send_message("false", cli->name);
					}
					else{
						send_message("truee", cli->name);
						
						break;
					}
					
				}
				recv(cli->sockfd, password, 32, 0);
				cf = fopen(filename,"w+");
				fputs(cli->name,cf);
				fputs("\n",cf);
				fputs(password,cf);
				fputs("\n",cf);
				fclose(cf);
				break;
			}
			else if(!strcmp(number,"3")){
				close(cli->sockfd);
				queue_remove(cli->uid);
				free(cli);
				cli_count--;
				pthread_detach(pthread_self());
				return NULL;
			}
		}
	}

	while(1){
		if(recv(cli->sockfd, number, 32, 0) <= 0){
			printf("Didn't enter the number.\n");
		} else{
		
			if(!strcmp(number,"1")){// ADD FIREND
				while(1){
					
					recv(cli->sockfd, name, 32, 0);
					memset(filename, 0, sizeof(filename));
					memset(friend_filename, 0, sizeof(friend_filename));
					exist_flag=0;
					strcat(filename,cli->name);
					strcat(filename,txt);
					str_trim_lf(name, strlen(name));
					if(!strcmp(name,"exit")){
						send_message("truee", cli->name);
						
						break;
					}
					strcat(friend_filename,name);
					strcat(friend_filename,txt);
					if(exists(friend_filename)==0){
						send_message("false", cli->name);
					}
					else{
						cf = fopen(filename,"r");
						fgets(friend, 32, cf);
						fgets(friend, 32, cf);
						while(!feof(cf)){
							fgets(friend, 32, cf);
							str_trim_lf(friend, strlen(friend));
							strcpy(last_friend,friend);
							if(!strcmp(name,friend)){
								exist_flag = 1;
							}
						}
						fclose(cf);
						if(exist_flag==1){
							send_message("exist", cli->name);
						}
						else{
							cf = fopen(filename,"a");
							fgets(password,32, cf);
							fputs(name,cf);
							fputs("\n",cf);
							fclose(cf);
							send_message("truee", cli->name);
							
							break;
						}
					}
					
				}
			}
			else if(!strcmp(number,"2")){ //CHOOSE FRIEND
				memset(filename, 0, sizeof(filename));
				strcat(filename,cli->name);
				strcat(filename,txt);
				cf = fopen(filename,"r");
				fscanf(cf, "%s", friend);
				fscanf(cf, "%s", friend);
				while( fscanf(cf, "%s", friend) != EOF ){
					
					send_message("truee", cli->name);//sending information about more names
					str_trim_lf(friend, strlen(friend));
					recv(cli->sockfd, name, 32, 0);//conformation
					send_message(friend,cli->name);//sending name
					recv(cli->sockfd, name, 32, 0);//conformation
					
				}
				send_message("false", cli->name);
				recv(cli->sockfd, name, 32, 0);//conformation
				
				fclose(cf);
				
				while(1){
					
					recv(cli->sockfd, name, 32, 0);
					memset(filename, 0, sizeof(filename));
					memset(friend_filename, 0, sizeof(friend_filename));
					exist_flag=0;
					strcat(filename,cli->name);
					strcat(filename,txt);
					str_trim_lf(name, strlen(name));
					if(!strcmp(name,"exit")){
						send_message("exitt", cli->name);
						
						break;
					}
					strcat(friend_filename,name);
					strcat(friend_filename,txt);
					if(exists(friend_filename)==0){ //checking if profile exists
						send_message("false", cli->name);
						printf("xD1\n");
					}
					else{
						send_message("truee",cli->name);
						printf("xD2\n");
						bzero(buff_out, BUFFER_SZ);
						while(1){
							if (leave_flag) {
								break;
							}
							int receive = recv(cli->sockfd, buff_out, BUFFER_SZ, 0);
							if (receive > 0){
								if(strlen(buff_out) > 0){
									
									send_message(buff_out, name);
									
									str_trim_lf(buff_out, strlen(buff_out));
									printf("%s\n", buff_out);
								}
							} else if (receive == 0 || strcmp(buff_out, "exit") == 0){
								sprintf(buff_out, "%s has left\n", cli->name);
								printf("%s", buff_out);
								
								send_message(buff_out, name);
								
								leave_flag = 1;
							} else {
								printf("ERROR: -1\n");
								leave_flag = 1;
							}
							bzero(buff_out, BUFFER_SZ);
						}
					}

				}
				
			}
			else if(!strcmp(number,"3")){ //DELETE FRIEND
				while(1){
					
					recv(cli->sockfd, name, 32, 0);
					memset(filename, 0, sizeof(filename));
					memset(friend_filename, 0, sizeof(friend_filename));
					exist_flag=0;
					strcat(filename,cli->name);
					strcat(filename,txt);
					str_trim_lf(name, strlen(name));
					if(!strcmp(name,"exit")){
						send_message("truee", cli->name);
						
						break;
					}
					strcat(friend_filename,name);
					strcat(friend_filename,txt);
					if(exists(friend_filename)==0){ //checking if profile exists
						send_message("false", cli->name);
						
					}
					else{
						cf = fopen(filename,"r");
						fgets(friend, 32, cf);
						fgets(friend, 32, cf);
						while(!feof(cf)){
							str_trim_lf(friend, strlen(friend));
							strcpy(last_friend,friend);
							if(!strcmp(name,friend)){
								exist_flag = 1;
							}
							fgets(friend, 32, cf);
						}
						fclose(cf);
						if(exist_flag == 0){ //profile is not a friend
							send_message("frien", cli->name);
						}
						else{
							cf = fopen(filename,"r");
							temp_cf = fopen(temp_filename, "w+");
							fgets(friend, 32, cf);
							fputs(friend, temp_cf);
							fgets(friend, 32, cf);
							fputs(friend, temp_cf);
							while(!feof(cf)){
								fgets(friend, 32, cf);
								str_trim_lf(friend, strlen(friend));
								if(strcmp(friend,name)){
									fputs(friend, temp_cf);
									fputs("\n", temp_cf);
								}
							}
							fclose(cf);
							fclose(temp_cf);
							remove(filename);
							rename(temp_filename,filename);
							send_message("truee", cli->name);
							
							break;
						}
					}
					
				}
			}
			else if(!strcmp(number,"4")){
				close(cli->sockfd);
				queue_remove(cli->uid);
				free(cli);
				cli_count--;
				pthread_detach(pthread_self());
				return NULL;
			}
		}
	}
	
  /* Delete client from queue and yield thread */
  fclose(cf);
  close(cli->sockfd);
  queue_remove(cli->uid);
  free(cli);
  cli_count--;
  pthread_detach(pthread_self());

	return NULL;
}

int main(int argc, char **argv){
	if(argc != 2){
		printf("Usage: %s <port>\n", argv[0]);
		return EXIT_FAILURE;
	}

	char *ip = "127.0.0.1";
	int port = atoi(argv[1]);
	int option = 1;
	int listenfd = 0, connfd = 0;
    struct sockaddr_in serv_addr;
    struct sockaddr_in cli_addr;
    pthread_t tid;

	queue_init();
    /* Socket settings */
    listenfd = socket(AF_INET, SOCK_STREAM, 0);
    serv_addr.sin_family = AF_INET;
    serv_addr.sin_addr.s_addr = inet_addr(ip);
    serv_addr.sin_port = htons(port);

    /* Ignore pipe signals */
	signal(SIGPIPE, SIG_IGN);

	if(setsockopt(listenfd, SOL_SOCKET,(SO_REUSEPORT | SO_REUSEADDR),(char*)&option,sizeof(option)) < 0){
		perror("ERROR: setsockopt failed");
    return EXIT_FAILURE;
	}

	/* Bind */
    if(bind(listenfd, (struct sockaddr*)&serv_addr, sizeof(serv_addr)) < 0) {
    perror("ERROR: Socket binding failed");
    return EXIT_FAILURE;
  }

    /* Listen */
    if (listen(listenfd, 10) < 0) {
    perror("ERROR: Socket listening failed");
    return EXIT_FAILURE;
	}
	printf("=== WELCOME TO THE CHATROOM ===\n");
	while(1){
		socklen_t clilen = sizeof(cli_addr);
		connfd = accept(listenfd, (struct sockaddr*)&cli_addr, &clilen);
		// Check if max clients is reached
		if((cli_count + 1) == MAX_CLIENTS){
			printf("Max clients reached. Rejected: ");
			print_client_addr(cli_addr);
			printf(":%d\n", cli_addr.sin_port);
			close(connfd);
			continue;
		}
		// Client settings
		client_t *cli = (client_t *)malloc(sizeof(client_t));
		cli->address = cli_addr;
		cli->sockfd = connfd;
		cli->uid = uid++;
		// Add client to the queue and fork thread
		queue_add(cli);
		pthread_create(&tid, NULL, &handle_client, (void*)cli);

		sleep(1);
	}

	return EXIT_SUCCESS;
}
