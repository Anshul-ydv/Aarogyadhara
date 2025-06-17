import java.util.Scanner;

public class prac1{
    public static void main(String[] args) {
        int n;
        Scanner scan = new Scanner(System.in);
        n = scan.nextInt();
        int arr[] = new int[n];
        for (int i =0 ;i<n;i++){
            arr[i]=scan.nextInt();
        }
        for (int j=0 ; j<n;j++){
            if (arr[j]==1){
                System.out.print("min index is "+j);
                break;
            }
        }
    }
}