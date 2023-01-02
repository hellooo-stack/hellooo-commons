package site.hellooo.commons.idgenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Test {
    public static void main(String[] args) {
        List<Integer> userIds = new ArrayList<>();
        userIds.add(89999442);
        userIds.add(987446272);
        userIds.add(763889234);
        userIds.add(847462221);

        userIds.forEach(userId -> {
            int hash = userId.hashCode();
            hash = Optional.of(hash)
                    .filter(h -> h > 0)
                    .orElse(-hash);
            System.out.println("userId: " + userId + " , hash is: " + hash % 6 + " , routing to physical table example_table_" + hash % 6);
        });
    }
}
