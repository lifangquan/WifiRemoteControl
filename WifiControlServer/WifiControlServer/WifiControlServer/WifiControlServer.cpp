#include "stdafx.h"
#include <stdio.h> 
#include <Winsock2.h> 
#include <ws2tcpip.h>
#include <stdlib.h>
#pragma comment(lib,"WS2_32.lib")

#define MCASTADDR "224.0.0.1" //����ʹ�õĶಥ���ַ��
#define MCASTPORT 12341 //�󶨵ı��ض˿ںš�
#define BUFSIZE 1024 //�������ݻ����С��
int main(int argc, char ** argv)
{
	WSADATA wsd;
	struct sockaddr_in local, remote, from;
	SOCKET sock, sockM;
	char recvbuf[BUFSIZE];

	int len = sizeof(struct sockaddr_in);
	int ret;
	//��ʼ��WinSock2.2
	if (WSAStartup(MAKEWORD(2, 2), &wsd) != 0)
	{
		printf("WSAStartup() failed\n");
		return -1;
	}
	/*
	 ����һ��SOCK_DGRAM���͵�SOCKET
	  ����,WSA_FLAG_MULTIPOINT_C_LEAF��ʾIP�ಥ�ڿ������������"�޸�"����;
	   WSA_FLAG_MULTIPOINT_D_LEAF��ʾIP�ಥ���������������"�޸�",�йؿ�������
	��������йظ��������MSDN˵����
	 */
	if ((sock = WSASocket(AF_INET, SOCK_DGRAM, 0, NULL, 0,
		WSA_FLAG_MULTIPOINT_C_LEAF | WSA_FLAG_MULTIPOINT_D_LEAF |
		WSA_FLAG_OVERLAPPED)) == INVALID_SOCKET)
	{
		printf("socket failed with:%d\n", WSAGetLastError());
		WSACleanup();
		return -1;
	}
	//��sock�󶨵�����ĳ�˿��ϡ�
	local.sin_family = AF_INET;
	local.sin_port = htons(MCASTPORT);
	local.sin_addr.s_addr = INADDR_ANY;
	if (bind(sock, (struct sockaddr*)&local, sizeof(local)) == SOCKET_ERROR)
	{
		printf("bind failed with:%d \n", WSAGetLastError());
		closesocket(sock);
		WSACleanup();
		return -1;
	}
	//����ಥ��
	remote.sin_family = AF_INET;
	remote.sin_port = htons(MCASTPORT);
	remote.sin_addr.s_addr = inet_addr(MCASTADDR);
	/* Winsock1.0 */
	 /*
	  mcast.imr_multiaddr.s_addr = inet_addr(MCASTADDR);
	   mcast.imr_interface.s_addr = INADDR_ANY;
	if( setsockopt(sockM,IPPROTO_IP,IP_ADD_MEMBERSHIP,(char*)&mcast,sizeof(mcast)) == SOCKET_ERROR)
	 {
	  printf("setsockopt(IP_ADD_MEMBERSHIP) failed:%d\n",WSAGetLastError());
	closesocket(sockM);
	  WSACleanup();
	return -1;
	  }
	   */
	   /* Winsock2.0*/
	if ((sockM = WSAJoinLeaf(sock, (SOCKADDR*)&remote, sizeof(remote), NULL, NULL, NULL, NULL,
		JL_BOTH)) == INVALID_SOCKET)
	{
		printf("WSAJoinLeaf() failed:%d\n", WSAGetLastError());
		closesocket(sock);
		WSACleanup();
		return -1;
	}
	//���նಥ���ݣ������յ�������Ϊ"QUIT"ʱ�˳���
	while (1)
	{
		if ((ret = recvfrom(sock, recvbuf, BUFSIZE, 0, (struct sockaddr*)&from, &len)) == SOCKET_ERROR)
		{
			printf("recvfrom failed with:%d\n", WSAGetLastError());
			closesocket(sockM);
			closesocket(sock);
			WSACleanup();
			return -1;
		}
		if (strcmp(recvbuf, "ACTION_SCANF") == 0)
		{
			sendto(sock, (char*)"qwerty", strlen("qwerty"), 0, (struct sockaddr*)&from, sizeof(from));
		}
		else {
			recvbuf[ret] = '\0';
			char action[200];
			float x, y;
			sscanf(recvbuf, "%s : %f,%f", action, &x, &y);
			printf("%s %2.1f %2.1f\n", action,x,y);
			if (strcmp(action, "ACTION_MOVE") == 0)
			{
				mouse_event(MOUSEEVENTF_MOVE, x, y,0, GetMessageExtraInfo());
			}
			else if (strcmp(action, "ACTION_DOWN") == 0)
			{
				mouse_event(MOUSEEVENTF_LEFTDOWN, 0, 0, 0, GetMessageExtraInfo());
			}
			else if (strcmp(action, "ACTION_UP") == 0)
			{
				mouse_event(MOUSEEVENTF_LEFTUP, 0, 0, 0, GetMessageExtraInfo());
			}
		}
	}

	closesocket(sockM);
	closesocket(sock);
	WSACleanup();
	return 0;
}