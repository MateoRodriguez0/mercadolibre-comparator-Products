package com.compare.products.commercial.information.services.implementations;


import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.compare.products.commercial.information.models.CommercialInformation;
import com.compare.products.commercial.information.models.Shipping;
import com.compare.products.commercial.information.models.ShippingMode;
import com.compare.products.commercial.information.models.Warranty;
import com.compare.products.commercial.information.services.InformationCommercialService;
import com.compare.products.commercial.information.services.PaymentMethodsService;
import com.compare.products.commercial.information.services.ShippingService;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class InformationItem implements InformationCommercialService {

	@Override
	public CommercialInformation getInfoCommercial(JsonNode jsonNode) {
		CommercialInformation information = new CommercialInformation();
		information.setRating_average(getRatingAverage(jsonNode.get(itemId).asText()));
		information.setPayment_methods(paymentMethodsService.findPaymentMethods());
		information.setDiscount_porcentage(getDiscount(jsonNode));
		information.setPrice(jsonNode.get(ItemPrice).asDouble());
		information.setWarranty(getWarranty(jsonNode));
		information.setCurrency_id(jsonNode.get(currency).asText());
		information.setShipping(getShipping(jsonNode));
		if(information.getShipping().getCosts()!=null) {
			information.getShipping().setCurrency(jsonNode.get(currency).asText());
		}
		
		Document document =null;
		try {
			document = Jsoup.connect(jsonNode.at(permalink).asText()).get();
		} catch (IOException e) {
			
		}
		try {
			String sales=document.getElementsByClass(salesTag)
					.text()
					.replaceAll("[a-zA-Z]|\s|\\|","");
			information.setTotal_sales(sales.length()!=0 ? sales : null);
		} catch ( NumberFormatException e) {
			
		}
		try {
			String available=document.getElementsByClass(availableTag)
					.text()
					.replaceAll("[a-zA-Z]|\s|[(]|[)]","");
			information.setAvailables(available.length()!=0 ? available : null);
			
		} catch (NumberFormatException e) {
			
		}
		
		for (JsonNode atr : jsonNode.get(attributes)) {
			if(atr.get("id").asText().equals(brandId)) {
				information.setBrand(atr.get(brandName).asText());
				break;
			}
		}
		return information;
	}
	

	@Override
	public Warranty getWarranty(JsonNode jsonNode) {
		for (JsonNode term : jsonNode.get(saleTerms)) {
			if(term.get("id").asText().equals(saleTermsNameWarranty)) {
				return Warranty.builder()
						.number(term.at(warrantyNumber).asInt())
						.unit(term.at(warrantyUnit).asText())
						.build();
			}
		}
		return null;
	}

	@Override
	public Shipping getShipping(JsonNode jsonNode) {
		if(jsonNode.at(freshipping).asBoolean()) {
			return Shipping.builder().mode(ShippingMode.free).build();
		}
		return shippingService.getShippingItem(jsonNode.get(itemId).asText());	
	}


	@Override
	public int getDiscount(JsonNode jsonNode) {
		int orignalPrice=jsonNode.get(ItemPriceOriginal).asInt();
		int currentPrice=jsonNode.get(ItemPrice).asInt();
		if(orignalPrice!=0) {
			return (int)(((double)(orignalPrice-currentPrice)/orignalPrice)*100);
		}
		return 0;
			
	}
	
	
	@Override
	public double getRatingAverage(String id) {
		RequestEntity<?> entity= RequestEntity.get(ratingUrl.replace("{id}", id))
				.header("Authorization", request.getHeader("Authorization"))
				.build();
		ResponseEntity<JsonNode> response=clientHttp.exchange(entity, JsonNode.class);
		return response.getBody().at(rating).asDouble();
	}
	
	
	@Autowired
	private ShippingService shippingService;
	
	@Value("${compare.products.paths.rating-for-item}")
	private String ratingUrl;
	
	@Value("${compare.products.paths.scraper.reviews}")
	private String reviewsUrl;
	
	@Value("${json.properties.item.free-shipping}")
	private String freshipping;
	
	@Value("${compare.products.scraper.tags.quantity-available}")
	private String availableTag;
	
	@Value("${compare.products.scraper.tags.review-calification}")
	private String calificationTag;
	
	@Value("${compare.products.scraper.tags.sales}")
	private String salesTag;

	@Value("${json.properties.item.price}")
	private String ItemPrice;
	
	@Value("${json.properties.item.original_price}")
	private String ItemPriceOriginal;

	@Value("${json.properties.item.id}")
	private String itemId;
	
	@Value("${json.properties.reviews-item.rating}")
	private String rating;
	
	@Value("${json.properties.item.curency_id}")
	private String currency;
	
	@Value("${json.properties.item.attributes}")
	private String attributes;
	@Value("${json.properties.item.brand_id}")
	private String brandId;
	
	@Value("${json.properties.item.brand_name}")
	private String brandName;
	
	@Value("${json.properties.item.warranty}")
	private String saleTerms;
	
	@Value("${json.properties.item.name-terminos-venta}")
	private String saleTermsNameWarranty;
	
	@Value("${json.properties.item.warranty-unit}")
	private String warrantyUnit;
	
	@Value("${json.properties.item.warranty-number}")
	private String warrantyNumber;
	
	@Value("${json.properties.item.permalink}")
	private String permalink;
	
	@Autowired
	private RestTemplate clientHttp;

	@Autowired
	private PaymentMethodsService paymentMethodsService;
	
	@Autowired
	private HttpServletRequest request;


	
}