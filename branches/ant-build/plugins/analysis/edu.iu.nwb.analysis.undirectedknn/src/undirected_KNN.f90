       program undirected_KNN

  
!
!      It calculates the degree-degree correlations 
!      for an undirected network 
!      whose matrix is given as input as list of all edges
!      The correlation coefficient is knn
!      

       implicit none

       integer i,j,k,maxdeg,mindeg,n_vert,n_edges,n_bins,i1,i2,minind,maxind
       integer nattrN,nattrE,n_edges_N,n_edges_E,n_vert0,ch,n_vert1,i0
       integer, allocatable, dimension (:) :: degree,np,ind,indc,nodes
       integer, allocatable, dimension (:) :: count_deg
       real*8, allocatable, dimension (:) :: knn,avdegbin,interv,avoutbin
       real*8, allocatable, dimension (:) :: knn_his
       logical, allocatable, dimension(:):: nodelist
       real*8 binsize,avk,avk2
       character*256 filename,fileout1,fileout2,fileout3,sn_bins
       character*50 str(1:20),headattrN(1:20),headattrE(1:20),str1,str2,str3
       character*25,allocatable,dimension(:,:):: attrN,attrE
       
       logical quoteit
       character*(25) addquotes
      
!      Here the program reads the input parameters
       
       call GETARG(2,sn_bins)
       call GETARG(4,filename)
       read(sn_bins,*)n_bins
       
       fileout1='knn.dat'
       fileout2='knn_binned.dat'
       fileout3='network_undirected_knn.nwb'

!      Here the arrays are allocated

       n_edges=0
       n_vert0=0
       n_vert1=0
       maxind=1
       minind=10000000
       ch=0
       n_edges_N=0
       n_edges_E=0
       nattrN=0

       open(20,file=filename,status='unknown')
       do 
          read(20,106,err=8103,end=8103)str1
          if(str1(1:1)=='*'.AND.str1(2:2)=='N')then
             read(20,*)str3
             if(str3(1:1)=='*')then
                ch=1
                goto 8103
             endif
             read(20,*)str2
             backspace(20)
             backspace(20)
             do k=1,20
                read(20,*)(str(j),j=1,k)
                if(str(k)(1:1)==str2(1:1))exit
                backspace(20)
             enddo
             backspace(20)
             nattrN=k-1
             do 
                read(20,*,err=8103,end=8103)str2
                if(str2(1:1)=='*')goto 8103
                n_edges_N=n_edges_N+1  
             enddo
          else if(str1(1:1)=='*'.AND.str1(2:2)=='U')then 
             read(20,*)
             read(20,*)str2
             backspace(20)
             backspace(20)
             do k=1,20
                read(20,*)(str(j),j=1,k)
                if(str(k)(1:1)==str2(1:1))exit
                backspace(20)
             enddo
             backspace(20)
             nattrE=k-1
             do 
                read(20,*,err=9103,end=9103)
                n_edges_E=n_edges_E+1   
             enddo
          endif
       enddo
8103   continue
       backspace(20)
       do
          read(20,106,err=9103,end=9103)str1
          if(str1(1:1)=='*'.AND.str1(2:2)=='U')then
             read(20,*)
             read(20,*)str2
             backspace(20)
             backspace(20)
             do k=1,20
                read(20,*)(str(j),j=1,k)
                if(str(k)(1:1)==str2(1:1))exit
                backspace(20)
             enddo
             backspace(20)
             nattrE=k-1
             do 
                read(20,*,err=9103,end=9103)
                n_edges_E=n_edges_E+1   
             enddo
          endif
       enddo
9103   continue
       close(20)
       allocate(nodes(1:n_edges_N))
       allocate(ind(1:n_edges_E))
       allocate(indc(1:n_edges_E))
       allocate(attrE(1:nattrE-2,1:n_edges_E))
       if(nattrN>1)then
          allocate(attrN(1:nattrN-1,1:n_edges_N))
       endif
       open(20,file=filename,status='unknown')
       do 
          read(20,106,err=8104,end=8104)str1
          if(str1(1:1)=='*'.AND.str1(2:2)=='N')then
             if(ch==1)then
                read(20,*)
                goto 8104
             endif
             read(20,*)(headattrN(i),i=1,nattrN)
             do k=1,n_edges_N
                read(20,*,err=8114,end=8114)nodes(n_vert0+1),(attrN(j,n_vert0+1),j=1,nattrN-1)
                do i = 1, nattrN - 1, 1
                   if(quoteit(headattrN(i+1),attrN(i, n_vert0+1))) then
                       attrN(i, n_vert0+1) = addquotes(attrN(i, n_vert0+1))
                   endif
                enddo
                n_vert0=n_vert0+1
                if(minind>nodes(n_vert0))minind=nodes(n_vert0)
                if(maxind<nodes(n_vert0))maxind=nodes(n_vert0)  
8114            continue
             enddo
             goto 8104
          else if(str1(1:1)=='*'.AND.str1(2:2)=='U')then 
             read(20,*)(headattrE(i),i=1,nattrE)
             do k=1,n_edges_E
                read(20,*,err=9114,end=9114)indc(n_edges+1),ind(n_edges+1),(attrE(j,n_edges+1),j=1,nattrE-2)
                do i = 1, nattrE -1, 1
                	if(quoteit(headattrE(i+2), attrE(i, n_edges+1))) then
                		attrE(i, n_edges+1) = addquotes(attrE(i, n_edges+1))
                	endif
                enddo
                n_edges=n_edges+1
                if(minind>indc(n_edges))minind=indc(n_edges)
                if(minind>ind(n_edges))minind=ind(n_edges)
                if(maxind<ind(n_edges))maxind=ind(n_edges)
                if(maxind<indc(n_edges))maxind=indc(n_edges)
9114            continue
             enddo
             goto 9104
          endif
       enddo
8104   continue
       backspace(20)
       do
          read(20,106,err=9104,end=9104)str1
          if(str1(1:1)=='*'.AND.str1(2:2)=='U')then
             read(20,*)(headattrE(i),i=1,nattrE)
             do k=1,n_edges_E
                read(20,*,err=9214,end=9214)indc(n_edges+1),ind(n_edges+1),(attrE(j,n_edges+1),j=1,nattrE-2)
                do i = 1, nattrE -1, 1
                	if(quoteit(headattrE(i+2), attrE(i, n_edges+1))) then
                		attrE(i, n_edges+1) = addquotes(attrE(i, n_edges+1))
                	endif
                enddo
                n_edges=n_edges+1
                if(minind>indc(n_edges))minind=indc(n_edges)
                if(minind>ind(n_edges))minind=ind(n_edges)
                if(maxind<ind(n_edges))maxind=ind(n_edges)
                if(maxind<indc(n_edges))maxind=indc(n_edges)
9214            continue
             enddo
          endif
       enddo
9104   continue
       close(20)

       if(n_edges==0)then
          write(*,*)'Error! The program should be applied on undirected networks'
          stop
       endif
       
       allocate(nodelist(minind:maxind))
       allocate(degree(minind:maxind))
       degree=0
       nodelist=.false.
       do i=1,n_vert0
          nodelist(nodes(i))=.true.
       enddo
       n_vert=n_vert0
       do i=1,n_edges
          if(nodelist(ind(i)).eqv..false.)then
             nodelist(ind(i))=.true.
             n_vert=n_vert+1
          endif
          if(nodelist(indc(i)).eqv..false.)then
             nodelist(indc(i))=.true.
             n_vert=n_vert+1
          endif
          degree(indc(i))=degree(indc(i))+1
          degree(ind(i))=degree(ind(i))+1
       enddo
       if(n_vert0<n_vert)then
          print*,'The nwb file is not properly formatted: not all nodes/labels are listed'
          stop
       endif

       n_vert1=n_vert0
       n_vert0=0

       allocate(knn(minind:maxind))
       allocate(np(1:n_bins))
       allocate(avdegbin(1:n_bins))
       allocate(avoutbin(1:n_bins))
       allocate(interv(0:n_bins))
       
!      Here we initialize to zero the degrees of the nodes


!      Here we calculate the minimum and maximum degree, allocate and 
!      initialize the histogram of the degree-degree correlations 
!      (array knn_his)

       mindeg=1000000
       maxdeg=0

       do i=minind,maxind
          if(nodelist(i).eqv..true.)then
             if(degree(i)>maxdeg)maxdeg=degree(i)
             if(degree(i)<mindeg)mindeg=degree(i)
          endif
       enddo
       
       allocate(knn_his(mindeg:maxdeg))
       allocate(count_deg(mindeg:maxdeg))

!      Here we calculate the correlation coefficients

       knn=0.0d0

       do j=1,n_edges
          knn(ind(j))=knn(ind(j))+real(degree(indc(j)))/degree(ind(j))
          knn(indc(j))=knn(indc(j))+real(degree(ind(j)))/degree(indc(j))
       enddo


!      Here we average the correlation coefficients among nodes with equal degree
!      (array knn_his)
!      and write out the final averages 

       knn_his=0.0d0
       count_deg=0
       avk=0.0d0
       avk2=0.0d0

       do k=minind,maxind
          if(nodelist(k).eqv..true.)then
             n_vert0=n_vert0+1
             nodes(n_vert0)=k
             knn_his(degree(k))=knn_his(degree(k))+knn(k)
             count_deg(degree(k))=count_deg(degree(k))+1
             avk=avk+real(degree(k))/n_vert
             avk2=avk2+(real(degree(k))/n_vert)*degree(k)
          endif
       enddo

       open(21,file=fileout1,status='unknown')
       write(21,103)'# Nodes ',n_vert
       write(21,*)'#   Degree |   knn'
       write(21,*)

       do k=mindeg,maxdeg
          if(count_deg(k)>0)then
             write(21,107)k,(knn_his(k)*avk)/(count_deg(k)*avk2)
          endif
       enddo

       close(21)

       if(mindeg==0)then
          interv(0)=real(mindeg+1)
       else
          interv(0)=real(mindeg)
       endif

       binsize=(log(real(maxdeg)+0.1d0)-log(interv(0)))/n_bins
       
       do i=1,n_bins
          interv(i)=exp(log(interv(i-1))+binsize)
       enddo

       avdegbin=0.0d0
       avoutbin=0.0d0
       np=0
       
       do i=minind,maxind
          if(nodelist(i).eqv..true.)then
             do j=1,n_bins
                if(real(degree(i))<interv(j))then
                   np(j)=np(j)+1
                   avdegbin(j)=avdegbin(j)+real(degree(i))
                   avoutbin(j)=avoutbin(j)+knn(i)
                   exit
                endif
             enddo
          endif
       enddo

       do i=1,n_bins
          if(np(i)>0)then
             avdegbin(i)=avdegbin(i)/np(i)
             avoutbin(i)=avoutbin(i)/np(i)
          endif
       enddo

       open(20,file=fileout2,status='unknown')
       write(20,103)'# Nodes ',n_vert
       write(20,*)'#   Degree |   knn'
       write(20,*)
       do i=1,n_bins
          if(np(i)>0)then
             write(20,104)avdegbin(i),(avoutbin(i)*avk)/avk2
          endif
       enddo
       close(20)

       open(20,file=fileout3,status='unknown')
       write(20,112)'*Nodes'
       write(20,109)(headattrN(i),i=1,nattrN),'knn*real'
       if(nattrN-1==1)then
          do i=1,n_vert
             write(20,120)nodes(i),(attrN(j,i),j=1,nattrN-1),knn(nodes(i))
          enddo
       else if(nattrN-1==2)then
          do i=1,n_vert
             write(20,121)nodes(i),(attrN(j,i),j=1,nattrN-1),knn(nodes(i))
          enddo
       else if(nattrN-1==3)then
          do i=1,n_vert
             write(20,122)nodes(i),(attrN(j,i),j=1,nattrN-1),knn(nodes(i))
          enddo
       else if(nattrN-1==4)then
          do i=1,n_vert
             write(20,123)nodes(i),(attrN(j,i),j=1,nattrN-1),knn(nodes(i))
          enddo
       else if(nattrN-1==5)then
          do i=1,n_vert
             write(20,124)nodes(i),(attrN(j,i),j=1,nattrN-1),knn(nodes(i))
          enddo
       else if(nattrN-1==6)then
          do i=1,n_vert
             write(20,125)nodes(i),(attrN(j,i),j=1,nattrN-1),knn(nodes(i))
          enddo
       else if(nattrN-1==7)then
          do i=1,n_vert
             write(20,126)nodes(i),(attrN(j,i),j=1,nattrN-1),knn(nodes(i))
          enddo
       else if(nattrN-1==8)then
          do i=1,n_vert
             write(20,127)nodes(i),(attrN(j,i),j=1,nattrN-1),knn(nodes(i))
          enddo
       else if(nattrN-1==9)then
          do i=1,n_vert
             write(20,128)nodes(i),(attrN(j,i),j=1,nattrN-1),knn(nodes(i))
          enddo
       else if(nattrN-1==10)then
          do i=1,n_vert
             write(20,129)nodes(i),(attrN(j,i),j=1,nattrN-1),knn(nodes(i))
          enddo
       endif
       write(20,113)'*UndirectedEdges'
       write(20,109)(headattrE(i),i=1,nattrE)
       do i=1,n_edges
          write(20,110)indc(i),ind(i),(attrE(j,i),j=1,nattrE-2)
       enddo
       close(20)


101    format(a41)
102    format(3i12)
103    format(a8,i10)
104    format(8x,e15.6,6x,e15.6)
105    format(a40,e15.6)
106    format(a25)
107    format(i12,e15.6)
109    format(20a50)      
110    format(i10,8x,i10,1x,18a25)
111    format(i10,10x,20a25)
112    format(a6)
113    format(a16)
120    format(i10,2x,a25,1x,e15.6)
121    format(i10,2x,2a25,1x,e15.6)
122    format(i10,2x,3a25,1x,e15.6)
123    format(i10,2x,4a25,1x,e15.6)
124    format(i10,2x,5a25,1x,e15.6)
125    format(i10,2x,6a25,1x,e15.6)
126    format(i10,2x,7a25,1x,e15.6)
127    format(i10,2x,8a25,1x,e15.6)
128    format(i10,2x,9a25,1x,e15.6)
129    format(i10,2x,10a25,1x,e15.6)

       stop
     end program undirected_KNN

     
     logical function quoteit(header, value)
		character*(*) header, value
		if(index(header, '*string') > 0 .AND. '*' /= value) then
			quoteit = .TRUE.
		else
			quoteit = .FALSE.
		endif
		return
	end 
	
	character*(25) function addquotes(value)
		character*(*) value
		character*22 longname
		
		if(len(TRIM(ADJUSTL(value))) >= 23) then
			longname = ADJUSTL(value)
			addquotes = '"' // longname // '"'
		else
			addquotes = '"' // TRIM(ADJUSTL(value)) // '"'
		endif
		return
	end