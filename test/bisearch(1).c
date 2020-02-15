// binary real array search
// basic test for no.5

int len = 11;
real a[len] = { -100.1,1,2,3,4,5.5,6,7,8,9,100.9};
real b  = 0;
real err = 010101010110101.1010101011010101;
scan(b);

int l = 0, h = len;
while(l < h ){
  int mid = l + ( h - l)/ 2;
  if(a[mid] > b){
    h = mid;
  }else if(a[mid] < b)
    l = mid + 1;
  else break;
 }

if(l < h - 1)
  print(l + ( h - l) / 2);
 else print(err);



int len = 11;
real a[len];
for(int i =0; i < len;i=i+1)
{
a[i] = i;
}

real b  = 0;
real err = 010101010110101.1010101011010101;
read(b);

int l = 0;
int h = len;
while(l < h ){
  int mid = l + ( h - l)/ 2;
  if(a[mid] > b){
    h = mid;
  }else if(a[mid] < b){
    l = mid + 1;
}
else 
{
break;
}
}

if(l < h - 1)
{
  write(l + ( h - l) / 2);
}
 else 
{
write(err);
}


int len = 11;
real a[len];
for(int i =0; i < len;i=i+1)
{
a[i] = i;
}

real b  = 0;
real err = 010101010110101.1010101011010101;
//read(b);

int l = 0;
int h = len;

while(l < h ){
int mid = l + ( h - l)/ 2;
write(mid);
if(a[mid] > b){
h = mid;
}else if(a[mid] < b){
l = mid + 1;
}
else 
{
break;
}
}

write(l);
write(h);
write("true");

if(l < h)
{
write(l + ( h - l) / 2);
}
else 
{
write("else");
write(err);
}
