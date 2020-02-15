// insert sort
// basic test no.1
int n;
real err = 1111111111111.1111111;
scan(n);
if(n< 2)
print(err);
int a[n];
a[0] = 1;
for(int i = 1;i < n;i = i + 1){
  scan(a[i]);
  int j = i - 1;
  int i1 = i;
  while(j >= 0 && a[j] > a[i1]){
    int b = a[j];
    a[j] = a[i1];
    a[i1] = b;
    i1 = j;
    j = j - 1;
  }
}

for(int i = 0; i < n;i = i + 1)
  print(a[i]);

// print('\n');
