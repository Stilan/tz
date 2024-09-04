package model;

import java.time.LocalDate;

public class Purchase {

    private Customer customer;
    private Product product;
    private Double price;
    private LocalDate purchaseDate;

    public Purchase(Customer customer, Product product, Double price, LocalDate purchaseDate) {
        this.customer = customer;
        this.product = product;
        this.price = price;
        this.purchaseDate = purchaseDate;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }
}
