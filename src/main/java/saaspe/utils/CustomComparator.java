package saaspe.utils;

import java.util.Comparator;

import saaspe.model.RequestTrackingListViewResponse;

public class CustomComparator implements Comparator<RequestTrackingListViewResponse> {
	String category;

	public CustomComparator(String category) {
		this.category = category;
	}

	@Override
	public int compare(RequestTrackingListViewResponse firstobject, RequestTrackingListViewResponse secondobject){
		int startIndex;
		if (category.equalsIgnoreCase("project")) {
			startIndex = 6;
		}else if (category.equalsIgnoreCase("contract")) {
			startIndex = 10;
		}else
			startIndex = 9;
		String num1 = null;
		String num2 = null;
		int number1 = 0;
		int number2 = 0;
		int childRequest1 = 0;
		int childRequest2 = 0;
		number1 = Integer.parseInt(firstobject.getRequestId().substring(startIndex));
		if (firstobject.getChildRequestId() != null) {
			num1 = firstobject.getChildRequestId();
			childRequest1 = num1.lastIndexOf("_");
		}
		number2 = Integer.parseInt(secondobject.getRequestId().substring(startIndex));
		if (secondobject.getChildRequestId() != null) {
			num2 = secondobject.getChildRequestId();
			childRequest2 = num2.lastIndexOf("_");
		}
		if ((firstobject.getChildRequestId() == null && secondobject.getChildRequestId() == null)
				|| (firstobject.getChildRequestId() != null && secondobject.getChildRequestId() == null)
				|| (firstobject.getChildRequestId() == null && secondobject.getChildRequestId() != null)) {
			return number1 - number2;
		} else if (firstobject.getChildRequestId() != null && secondobject.getChildRequestId() != null && num1!=null && num2 !=null) {
			if (number1 != number2)
				return number1 - number2;
			else {
				number1 = Integer.parseInt(num1.substring(childRequest1 + 1));
				number2 = Integer.parseInt(num2.substring(childRequest2 + 1));
				return number1 - number2;
			}
		}
		return number1 - number2;
	}
}
