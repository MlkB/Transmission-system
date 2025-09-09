package sources;

import java.util.*;
import information.Information;

public class SourceAleatoire extends Source {
    
    public SourceAleatoire (){
        super();
    }
    public static String generateRandom(int length){
        Information<Boolean> infoEmise = new Information<>();
        Random randomChain = new Random();
        for (int i = 0; i < length; i++) {
            infoEmise.add(randomChain.nextBoolean());
        }
        StringBuilder sb = new StringBuilder();
        for (Boolean b : infoEmise) {
            sb.append(b ? "1" : "0");
        }
    
        return sb.toString();
    }
    public static void main(String args[]) {
        String info = generateRandom(8);
        System.out.println("The generated information is: " + info);
    }
}
