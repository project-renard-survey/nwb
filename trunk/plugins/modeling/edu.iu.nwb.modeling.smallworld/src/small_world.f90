      program small_world
!   
!     It builds a small world graph a la Watts-Strogatz: 
!     the rewiring probability is p
!
!   * degree is the array which stores the degree of each node,
!     which is printed out at the end in the file "degree.dat"


      implicit none
      integer*8 ibm
      integer i,j,k,vic,newvic,mindeg,maxdeg,n_vert,k_nei
      integer, allocatable,dimension(:)::linklist,degree,degdis
      integer, allocatable,dimension(:)::check_vic_in,check_vic_out
      real*8 r,p
      character*60 sn_vert,sk_nei,sp

!     Reading of input parameters 
      
      call GETARG(2,sn_vert)
      call GETARG(4,sk_nei)
      call GETARG(6,sp)

      read(sn_vert,*)n_vert
      read(sk_nei,*)k_nei
      read(sp,*)p

!     Array allocations

      allocate(linklist(1:n_vert*k_nei))
      allocate(degree(1:n_vert))
      allocate(check_vic_in(1:n_vert))
      allocate(check_vic_out(1:n_vert))

!     Opening of the file where the edges will be saved

      open(21,file='network.nwb',status='unknown')
      write(21,108)'// Barabasi-Albert network'
      write(21,102)'// Initial neighbors of each node ',k_nei
      write(21,110)'// Rewiring probability ',p
      write(21,103)'*Nodes ',n_vert
      write(21,109)'*UndirectedEdges'

!     Initialization of list of neighbors before rewiring

      do i=1,n_vert
         do j=1,k_nei
            vic=i+j
            if(vic>n_vert)vic=vic-n_vert
            linklist((i-1)*k_nei+j)=vic
         enddo
      enddo
      
!     ibm is the seed of the multiplicative random number generator used
!     in this program 

      ibm=10
      check_vic_in=0
      check_vic_out=0

!     Here we make the rewiring of the edges

      do j=1,k_nei
         do i=1,n_vert
            ibm=ibm*16807                                       !  Here we create a random number
            r=0.25d0*(ibm/2147483648.0d0/2147483648.0d0+2.0d0)  !  r between 0 and 1
            if(r<p)then
               do k=1,k_nei
                  check_vic_in(linklist((i-1)*k_nei+k))=1
               enddo
               check_vic_in(i)=1
               ibm=ibm*16807                                      
               r=0.25d0*(ibm/2147483648.0d0/2147483648.0d0+2.0d0)  
               newvic=ceiling(r*n_vert)
               if(check_vic_in(newvic)==0)then
                  do k=1,k_nei
                     check_vic_out(linklist((newvic-1)*k_nei+k))=1
                  enddo
                  if(check_vic_out(i)==0)then
                     linklist((i-1)*k_nei+j)=newvic
                  endif
                  do k=1,k_nei
                     check_vic_out(linklist((newvic-1)*k_nei+k))=0
                  enddo
               endif
               do k=1,k_nei
                  check_vic_in(linklist((i-1)*k_nei+k))=0
               enddo
               check_vic_in(i)=0
            endif
         enddo
      enddo

!     Here we calculate the degree of the nodes and write out the final 
!     network as list of its edges in the file "network"

      degree=0

      do i=1,n_vert
         do j=1,k_nei
            write(21,*)i,linklist((i-1)*k_nei+j)
            degree(i)=degree(i)+1
            degree(linklist((i-1)*k_nei+j))=degree(linklist((i-1)*k_nei+j))+1
         enddo
      enddo

      close(21)

!     Here we write out the degree of all vertices

      open(20,file='degree_small_world.dat',status='unknown')
      write(20,*)'# Small World network'
      write(20,102)'# Initial neighbors of each node ',k_nei
      write(20,110)'# Rewiring probability ',p
      write(20,103)'# Nodes ',n_vert
      write(20,*)'#     Node     |     Degree'
      write(20,*)
      do i=1,n_vert
         write(20,*)i,degree(i)
      enddo
      close(20)

!     Here we determine the minimum and maximum degree of the graph

      mindeg=MINVAL(degree)
      maxdeg=MAXVAL(degree)

!     Here we allocate the histogram of the degree distribution

      allocate(degdis(mindeg:maxdeg))

!     Here we initialize the histogram of the degree distribution
!     and evaluate the occurrence of each degree 

      degdis=0

      do i=1,n_vert
         degdis(degree(i))=degdis(degree(i))+1
      enddo
      
!     Here we write out the degree distribution: the occurrence of each degree is 
!     transformed in a probability by normalizing by the total degree 

      open(20,file='degree_distr_small_world.dat',status='unknown')
      write(20,*)'# Small world network'
      write(20,102)'# Initial neighbors of each node ',k_nei
      write(20,110)'# Rewiring probability ',p
      write(20,103)'# Nodes ',n_vert
      write(20,*)'#     Degree    |    Probability'
      write(20,*)
      do i=mindeg,maxdeg
         if(degdis(i)>0)then
            write(20,104)i,real(degdis(i))/n_vert
         endif
      enddo
      close(20)

101   format(i10,8x,i10)
102   format(a34,i10)
103   format(a6,i10)
104   format(i10,9x,e15.6)
105   format(4x,e15.6,4x,e15.6)
108   format(a22)
109   format(a16)
110   format(a24,e15.6)

9001  continue

      stop
    end program small_world
