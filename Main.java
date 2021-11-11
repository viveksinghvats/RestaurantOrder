import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

class Main {
    static int slotsOccupied = 0;
    // sort the orders in ascending on the basis of their total cooking and delivery time
    static PriorityQueue<OrderDetail> orderQueue = new PriorityQueue<>(new OrderCompare());
    public static void main(String[] args) {
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(System.in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuffer result = new StringBuffer();
            while (true) {
                String orderData = bufferedReader.readLine();
                if(orderData == null) break;
                result.append(calculateOrderTime(orderData.replaceAll("\\[|\\]", "").split("\\s*,\\s*"))).append(System.getProperty("line.separator"));
            }
            System.out.println(result);
        } catch (IOException e){
            System.out.println("Invalid Input Format");
        }
    }

    private static String calculateOrderTime(String[] inputOrderData){
        final int maxTimeLimit = 150;
        final int timePerKm = 8;
        final int maxCookingSlots = 7;
        final int totalEntry = inputOrderData.length;
        int slotsForOrder = 0;
        double timeToCookOrder = 0;
        String orderId = inputOrderData[0];
        double distance = Double.parseDouble(inputOrderData[totalEntry - 1]);
        // cannot cook more that 7 dishes for an order
        if(totalEntry > 9) { return String.format("Order %s is denied because the restaurant cannot accommodate it.", orderId); }
        for(int i = 1; i < totalEntry - 1; i++){
            if(slotsForOrder <= maxCookingSlots){
                if(inputOrderData[i].equals("A")){
                    slotsForOrder += 1;
                    // maximum cooking time for an order will be 29 minutes
                    timeToCookOrder = Math.max(timeToCookOrder, 17);
                } else if(inputOrderData[i].equals("M")){
                    slotsForOrder += 2;
                    timeToCookOrder = 29;
                } else { return String.format("Order %s is denied because the restaurant cannot accommodate it.", orderId); }
            } else { return String.format("Order %s is denied because the restaurant cannot accommodate it.", orderId); }
        }
        timeToCookOrder += (distance * timePerKm);
        if(slotsForOrder > maxCookingSlots) { return String.format("Order %s is denied because the restaurant cannot accommodate it.", orderId); }
        if(timeToCookOrder > maxTimeLimit) { return String.format("Order %s is denied because the restaurant cannot accommodate it.", orderId); }
        // more than one order can be cooked parallelly if enough free slots are available
        if((slotsOccupied + slotsForOrder) <= maxCookingSlots){
            slotsOccupied += slotsForOrder;
            orderQueue.add(new OrderDetail(slotsForOrder, timeToCookOrder));
        } else {
            // will wait for that order to be deliver till we have enough slots available for next order
            List<OrderDetail> orderDetails = new ArrayList<>();
            while(!orderQueue.isEmpty()){
                OrderDetail lastOrder = orderQueue.peek();
                slotsOccupied -= lastOrder.slots;
                if((slotsOccupied + slotsForOrder) <= maxCookingSlots){
                    timeToCookOrder += lastOrder.timeToCook;
                    slotsOccupied += slotsForOrder;
                    if(timeToCookOrder > maxTimeLimit){
                        slotsOccupied += lastOrder.slots;
                        orderDetails.stream().map(i -> slotsOccupied += i.slots).collect(Collectors.toList());
                        orderQueue.addAll(orderDetails);
                        return String.format("Order %s is denied because the restaurant cannot accommodate it.", orderId);
                    }
                    orderQueue.poll();
                    orderQueue.add(new OrderDetail(slotsForOrder, timeToCookOrder));
                    break;
                }
                orderDetails.add(orderQueue.poll());
            }
        }
        return String.format("Order %s will get delivered in %s minutes", orderId, timeToCookOrder);
    }

    private static class OrderCompare implements Comparator<OrderDetail> {
        public int compare(OrderDetail first, OrderDetail second) {
            if(first.timeToCook == second.timeToCook) return 0;
            else if(first.timeToCook > second.timeToCook) return 1;
            else return -1;
        }
    }

    private static class OrderDetail {
        int slots;
        double timeToCook;
        OrderDetail(int slots, double timeToCook){
            this.slots = slots;
            this.timeToCook = timeToCook;
        }
    }
}
