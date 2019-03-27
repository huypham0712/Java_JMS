import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderInformation {
    private String userReplyDestination;
    private List<SecondHandProduct> avaialbleProduct;

    public String getUserReplyDestination() {
        return userReplyDestination;
    }

    public List<SecondHandProduct> getAvaialbleProduct() {
        return avaialbleProduct;
    }

    public void setAvaialbleProduct(List<SecondHandProduct> avaialbleProduct) {
        this.avaialbleProduct = avaialbleProduct;
    }

    public OrderInformation() {
        this.userReplyDestination = "replyQueue" + UUID.randomUUID().toString();
        this.avaialbleProduct = new ArrayList<>();
    }
}
