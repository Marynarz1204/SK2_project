#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <signal.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <pthread.h>

#define LENGTH 2048

// Global variables
volatile sig_atomic_t flag = 0;
int sockfd = 0;
char name[32];
char password[32];
char number[32];
char friend[32];
char comformation[32];

void str_overwrite_stdout() {
  printf("%s", "> ");
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

void catch_ctrl_c_and_exit(int sig) {
    flag = 1;
}

void send_msg_handler() {
  char message[LENGTH] = {};
	char buffer[LENGTH + 32] = {};

  while(1) {
  	str_overwrite_stdout();
    fgets(message, LENGTH, stdin);
    str_trim_lf(message, LENGTH);

    if (strcmp(message, "exit") == 0) {
			break;
    } else {
		strcat(buffer,name);
		strcat(buffer,": ");
		strcat(buffer,message);
		strcat(buffer,"\n");
        send(sockfd, buffer, strlen(buffer), 0);
    }

		bzero(message, LENGTH);
    bzero(buffer, LENGTH + 32);
  }
  catch_ctrl_c_and_exit(2);
}

void send_number(){
	fgets(number, 32, stdin);
	str_trim_lf(number, strlen(number));
	send(sockfd, number, 32, 0);
}

void send_password(){
	fgets(password, 32, stdin);
	str_trim_lf(password, strlen(password));
	send(sockfd, password, 32, 0);
}

void send_name(){
	fgets(name, 32, stdin); //inserting name
	str_trim_lf(name, strlen(name));
	send(sockfd, name, 32, 0); //sending name
}

void send_conf(){
	send(sockfd, "sth", strlen("sth"), 0); //sending name
}


void recv_msg_handler() {
	char message[LENGTH] = {};
  while (1) {
		int receive = recv(sockfd, message, LENGTH, 0);
    if (receive > 0) {
      printf("%s", message);
      str_overwrite_stdout();
    } else if (receive == 0) {
			break;
    } else {
			// -1
		}
		memset(message, 0, sizeof(message));
  }
}

int main(int argc, char **argv){
	if(argc != 2){
		printf("Usage: %s <port>\n", argv[0]);
		return EXIT_FAILURE;
	}

	char *ip = argv[1];
	int port = 5454;

	signal(SIGINT, catch_ctrl_c_and_exit);

	struct sockaddr_in server_addr;

	/* Socket settings */
	sockfd = socket(AF_INET, SOCK_STREAM, 0);
  server_addr.sin_family = AF_INET;
  server_addr.sin_addr.s_addr = inet_addr(ip);
  server_addr.sin_port = htons(port);


  // Connect to Server
  int err = connect(sockfd, (struct sockaddr *)&server_addr, sizeof(server_addr));
  if (err == -1) {
		printf("ERROR: connect\n");
		return EXIT_FAILURE;
	}

	memset(comformation, 0, sizeof(comformation));

	printf("=== WELCOME ===\n");
	printf("1. Log in\n");
	printf("2. Sign up\n");
	printf("3. Exit\n");
	send_number();
	while(1){
		if(!strcmp(number,"1")){ //LOG IN
			strcpy(comformation,"false");
			printf("Please enter your name: ");
			send_name();
			recv(sockfd, comformation, 32, 0); //checking name
			str_trim_lf(comformation, strlen(comformation));
			while(!strcmp(comformation,"false")){
				printf("Profile does not exist.\n");
				printf("Please enter your name: ");
				send_name();
				recv(sockfd, comformation, 32, 0); //checking new name
			}
			strcpy(comformation,"false");
			printf("Please enter your password: ");
			send_password();
			recv(sockfd, comformation, 32, 0); //checking password
			str_trim_lf(comformation, strlen(comformation));
			while(!strcmp(comformation,"false")){
				printf("Wrong password\n");
				printf("Please enter your password: ");
				send_password();
				recv(sockfd, comformation, 32, 0); //checking new password
			}
			strcpy(comformation,"false");
			printf("Logged in!\n");
			break;
		}
		else if(!strcmp(number,"2")){ //SIGN UP
			printf("Please enter your new name: ");
			send_name();
			recv(sockfd, comformation, 32, 0); //checking new name
			str_trim_lf(comformation, strlen(comformation));
			while(!strcmp(comformation,"false")){
				printf("Profile name already exist.\n");
				printf("Please enter your name: ");
				send_name();
				recv(sockfd, comformation, 32, 0); //checking new name
			}
			strcpy(comformation,"false");
			printf("Please enter your password: ");
			send_password();
			
			break;
		}
		else if(!strcmp(number,"3")){ //EXIT
			printf("Closing\n");
			close(sockfd);
			return EXIT_SUCCESS;
		}
		else{
			printf("Wrong option try again\n");
			send_number();
		}
	}

	while(1){
		printf("=== WELCOME TO THE CHATROOM ===\n");
		printf("1. Add Friend\n");
		printf("2. Choose Friend\n");
		printf("3. Delete Friend\n");
		printf("4. Exit\n");

		send_number();
		while(1){
			if(!strcmp(number,"1")){ //ADD FRIEND
				printf("Enter friends name(or 'exit' to leave): ");
				send_name();
				recv(sockfd, comformation, 32, 0); //checking name
				str_trim_lf(comformation, strlen(comformation));
				while(!strcmp(comformation,"false") || !strcmp(comformation,"exist")){
					if(!strcmp(comformation,"false")){
						printf("Profile does not exist.\n");
					}
					else{
						printf("Already a friend.\n");
					}
					printf("Enter friends name: ");
					send_name();
					recv(sockfd, comformation, 32, 0); //checking new name
				}
				strcpy(comformation,"false");
				break;
			}
			else if(!strcmp(number,"2")){ //CHOOSE FRIEND
				printf("Choose friend to chat with:\n");
				memset(friend, 0 , sizeof(friend));
				while(1){
					memset(comformation, 0 , sizeof(comformation));
					recv(sockfd, comformation, 32, 0);
					send_conf();
					str_trim_lf(comformation, strlen(comformation));
					if(strcmp(comformation,"false")){
						memset(friend, 0, sizeof(friend));
						recv(sockfd, friend, 32, 0);
						send_conf();
						printf("%s\n",friend);
					}
					else{
						break;
					}
				}
				strcpy(comformation,"false");
				printf("or write 'exit' to go back: \n");
				fgets(friend, 32, stdin); //inserting name
				str_trim_lf(friend, strlen(friend));
				send(sockfd, friend, 32, 0); //sending name
				recv(sockfd, comformation, 32, 0);
				str_trim_lf(comformation, strlen(comformation));
				if(!strcmp(comformation,"exitt")){
					break;
				}
				else{
					while(!strcmp(comformation,"false")){
						printf("This profile is not your friend or does not exist\n");
						printf("Try again: \n");
						fgets(friend, 32, stdin); //inserting name
						str_trim_lf(friend, strlen(friend));
						send(sockfd, friend, 32, 0); //sending name
						recv(sockfd, comformation, 32, 0);
						printf("%s\n",comformation);
						str_trim_lf(comformation, strlen(comformation));
					}
					if(!strcmp(comformation,"truee")){
						printf("You are writing to %s", friend);
						pthread_t send_msg_thread;
						if(pthread_create(&send_msg_thread, NULL, (void *) send_msg_handler, NULL) != 0){
								printf("ERROR: pthread\n");
							return EXIT_FAILURE;
							}

						pthread_t recv_msg_thread;
						if(pthread_create(&recv_msg_thread, NULL, (void *) recv_msg_handler, NULL) != 0){
								printf("ERROR: pthread\n");
								return EXIT_FAILURE;
							}


							while (1){
								if(flag){
									printf("\nYou are no longer writing to %s\n",friend);
									break;
								}
							}
						break;

					}
					else{
						break;
					}
				}
				
			}
			else if(!strcmp(number,"3")){ //DELETE FRIEND
				printf("Enter friends name(or 'exit' to leave):");
				send_name();
				recv(sockfd, comformation, 32, 0); //checking name
				str_trim_lf(comformation, strlen(comformation));
				while(!strcmp(comformation,"false") || !strcmp(comformation,"frien")){
					if(!strcmp(comformation,"false")){
						printf("Profile does not exist.\n");
					}
					else{
						printf("Profile is not your friend.\n");
					}
					printf("Enter friends name:");
					send_name();
					recv(sockfd, comformation, 32, 0); //checking new name
				}
				strcpy(comformation,"false");
				break;
			}
			else if(!strcmp(number,"4")){ //EXIT
				printf("Closing\n");
				close(sockfd);
				return EXIT_SUCCESS;
			}
			else{
				printf("Wrong option try again\n");
				send_number();
			}
		}
	}



	

	
	close(sockfd);

	return EXIT_SUCCESS;
}
