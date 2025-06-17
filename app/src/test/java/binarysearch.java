import java.util.Scanner;

public class binarysearch {
    
    public static void main(String[] args) {
        int n;
        Scanner scan = new Scanner(System.in);
        n = scan.nextInt();
        int arr[] = new int[n];
        for (int i = 0; i < n; i++){
            arr[i] = scan.nextInt();
        }
        int left = 0;
        int right = n - 1;
        int minIndex = -1;
        while (left <= right){
            int mid = left + (right - left) / 2;
            if (arr[mid] == 1){
                minIndex = mid;
                right = mid - 1;
            }
            else {
                left = mid + 1;
            }
        }
        System.out.print(minIndex);
    }
}