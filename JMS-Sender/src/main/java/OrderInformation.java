import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderInformation {
    private String userReplyDestination;
    private String correlationId;
    private String messageDetail;
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

    public String getMessageDetail() {
        return messageDetail;
    }

    public void setMessageDetail(String messageDetail) {
        this.messageDetail = messageDetail;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public OrderInformation(String messageDetail, String userReplyDestination) {
        this.messageDetail = messageDetail;
        this.userReplyDestination = userReplyDestination;
        this.avaialbleProduct = new ArrayList<>();
    }
}
