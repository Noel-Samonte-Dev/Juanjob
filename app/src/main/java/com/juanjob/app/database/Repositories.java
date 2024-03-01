package com.juanjob.app.database;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import org.apache.commons.lang.RandomStringUtils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Repositories {

    DatabaseReference database;

    public interface RepoCallback {
        void onSuccess(String result);
    }

    public static String firebase_db_url = "https://juanjob-5e010-default-rtdb.asia-southeast1.firebasedatabase.app";


    //Key Generator - Hash
    private String primaryKeyGenerator(String id) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-hh:mm:ss-a");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 8);
        String date_time = dateFormat.format(calendar.getTime());
        String id_dateTime = getMd5(date_time);
        String s = null;
        if (id_dateTime.length() < 29) {
            int add_char = 29 - id_dateTime.length();
            s = RandomStringUtils.randomAlphanumeric(add_char).toUpperCase();
        } else {
            s = id_dateTime.substring(0, Math.min(id_dateTime.length(), 29));
        }
        return id + s;
    }

    private static String getMd5(String input) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] messageDigest = md.digest(input.getBytes());
        BigInteger no = new BigInteger(1, messageDigest);
        String hashtext = no.toString(16);
        while (hashtext.length() < 32) {
            hashtext = "0" + hashtext;
        }
        return hashtext;
    }


    //Customer
    public void createCustomer(CustomerTable customer, String customer_id, final RepoCallback callback) {
        database = FirebaseDatabase.getInstance(firebase_db_url)
                .getReference("customer_table")
                .child("customer_id")
                .child(customer_id);

        database.setValue(customer).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    callback.onSuccess("success");
                } else {
                    callback.onSuccess("failed");
                }
            }
        });
    }

    public void getCustomer(String customer_id, final RepoCallback callback) {
        database = FirebaseDatabase.getInstance(firebase_db_url)
                .getReference("customer_table").child("customer_id").child(customer_id);

        database.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful() && task.getResult().getValue() != null) {
                    String result = new Gson().toJson(task.getResult().getValue());
                    callback.onSuccess(result);
                } else {
                    callback.onSuccess("user_not_found");
                }
            }
        });
    }

    public void getAllCustomer(final RepoCallback callback) {
        database = FirebaseDatabase.getInstance(firebase_db_url)
                .getReference("customer_table");

        database.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful() && task.getResult().getValue() != null) {
                    String result = new Gson().toJson(task.getResult().getValue());
                    callback.onSuccess(result);
                } else {
                    callback.onSuccess("user_not_found");
                }
            }
        });
    }


    public void updateCustomer(CustomerTable customer, String customer_id,
                               final RepoCallback callback) {
        database = FirebaseDatabase.getInstance(firebase_db_url)
                .getReference("customer_table")
                .child("customer_id")
                .child(customer_id);

        database.setValue(customer).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    callback.onSuccess("success");
                } else {
                    callback.onSuccess("failed");
                }
            }
        });
    }

    public void updateCustomerOnlineStatus(String customer_id, String online_status,
                                           final RepoCallback callback) {
        database = FirebaseDatabase.getInstance(firebase_db_url)
                .getReference("customer_table")
                .child("customer_id")
                .child(customer_id)
                .child("online_status");

        database.setValue(online_status).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    callback.onSuccess("success");
                } else {
                    callback.onSuccess("failed");
                }
            }
        });
    }

    public void updateCustomerPassword(String customer_id, String password,
                                           final RepoCallback callback) {
        database = FirebaseDatabase.getInstance(firebase_db_url)
                .getReference("customer_table")
                .child("customer_id")
                .child(customer_id)
                .child("password");

        database.setValue(password).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    callback.onSuccess("success");
                } else {
                    callback.onSuccess("failed");
                }
            }
        });
    }


    //Client
    public void createClient(ClientTable client, String client_id, final RepoCallback callback) {
        database = FirebaseDatabase.getInstance(firebase_db_url)
                .getReference("client_table")
                .child("client_id")
                .child(client_id);

        database.setValue(client).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    callback.onSuccess("success");
                } else {
                    callback.onSuccess("failed");
                }
            }
        });
    }

    public void getClient(String client_id, final RepoCallback callback) {
        database = FirebaseDatabase.getInstance(firebase_db_url)
                .getReference("client_table")
                .child("client_id")
                .child(client_id);

        database.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful() && task.getResult().getValue() != null) {
                    String result = new Gson().toJson(task.getResult().getValue());
                    callback.onSuccess(result);
                } else {
                    callback.onSuccess("user_not_found");
                }
            }
        });
    }

    public void getAllClient(final RepoCallback callback) {
        database = FirebaseDatabase.getInstance(firebase_db_url)
                .getReference("client_table");

        database.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful() && task.getResult().getValue() != null) {
                    String result = new Gson().toJson(task.getResult().getValue());
                    callback.onSuccess(result);
                } else {
                    callback.onSuccess("user_not_found");
                }
            }
        });
    }

    public void getClientStatus(String client_id, final RepoCallback callback) {
        database = FirebaseDatabase.getInstance(firebase_db_url)
                .getReference("client_table")
                .child("client_id")
                .child(client_id)
                .child("online_status");

        database.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful() && task.getResult().getValue() != null) {
                    String result = new Gson().toJson(task.getResult().getValue());
                    callback.onSuccess(result);
                } else {
                    callback.onSuccess("user_not_found");
                }
            }
        });
    }

    public void updateClient(ClientTable client, String client_id, final RepoCallback callback) {
        database = FirebaseDatabase.getInstance(firebase_db_url)
                .getReference("client_table")
                .child("client_id")
                .child(client_id);

        database.setValue(client).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    callback.onSuccess("success");
                } else {
                    callback.onSuccess("failed");
                }
            }
        });
    }

    public void updateClientOnlineStatus(String client_id, String online_status,
                                         final RepoCallback callback) {
        database = FirebaseDatabase.getInstance(firebase_db_url)
                .getReference("client_table")
                .child("client_id")
                .child(client_id)
                .child("online_status");

        database.setValue(online_status).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    callback.onSuccess("success");
                } else {
                    callback.onSuccess("failed");
                }
            }
        });
    }

    public void updateClientPassword(String client_id, String password,
                                         final RepoCallback callback) {
        database = FirebaseDatabase.getInstance(firebase_db_url)
                .getReference("client_table")
                .child("client_id")
                .child(client_id)
                .child("password");

        database.setValue(password).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    callback.onSuccess("success");
                } else {
                    callback.onSuccess("failed");
                }
            }
        });
    }

    public void updateClientRating(String client_id, String rating,
                                     final RepoCallback callback) {
        database = FirebaseDatabase.getInstance(firebase_db_url)
                .getReference("client_table")
                .child("client_id")
                .child(client_id)
                .child("rating");

        database.setValue(rating).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    callback.onSuccess("success");
                } else {
                    callback.onSuccess("failed");
                }
            }
        });
    }

    public void updateClientRatingQuantity(String client_id, String quantity,
                                   final RepoCallback callback) {
        database = FirebaseDatabase.getInstance(firebase_db_url)
                .getReference("client_table")
                .child("client_id")
                .child(client_id)
                .child("rating_quantity");

        database.setValue(quantity).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    callback.onSuccess("success");
                } else {
                    callback.onSuccess("failed");
                }
            }
        });
    }

    public void updateClientCompletedOrders(String client_id, int completed_orders,
                                           final RepoCallback callback) {
        database = FirebaseDatabase.getInstance(firebase_db_url)
                .getReference("client_table")
                .child("client_id")
                .child(client_id)
                .child("completed_orders");

        database.setValue(completed_orders).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    callback.onSuccess("success");
                } else {
                    callback.onSuccess("failed");
                }
            }
        });
    }


    //Service
    public void createService(ServiceTable service, String client_id,
                              final RepoCallback callback) {
        database = FirebaseDatabase.getInstance(firebase_db_url)
                .getReference("service_table")
                .child("client_id")
                .child(client_id)
                .child("service_id")
                .child(primaryKeyGenerator("sku_"));

        database.setValue(service).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    callback.onSuccess("success");
                } else {
                    callback.onSuccess("failed");
                }
            }
        });
    }

    public void getService(String service_id, String client_id, final RepoCallback callback) {
        database = FirebaseDatabase.getInstance(firebase_db_url)
                .getReference("service_table")
                .child("client_id")
                .child(client_id)
                .child("service_id")
                .child(service_id);

        database.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful() && task.getResult().getValue() != null) {
                    String result = new Gson().toJson(task.getResult().getValue());
                    callback.onSuccess(result);
                } else {
                    callback.onSuccess("service_not_found");
                }
            }
        });
    }

    public void getAllClientServices(String client_id, final RepoCallback callback) {
        database = FirebaseDatabase.getInstance(firebase_db_url)
                .getReference("service_table")
                .child("client_id")
                .child(client_id);

        database.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful() && task.getResult().getValue() != null) {
                    String result = new Gson().toJson(task.getResult().getValue());
                    callback.onSuccess(result);
                } else {
                    callback.onSuccess("services_not_found");
                }
            }
        });
    }

    public void getAllServices(final RepoCallback callback) {
        database = FirebaseDatabase.getInstance(firebase_db_url)
                .getReference("service_table");

        database.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful() && task.getResult().getValue() != null) {
                    String result = new Gson().toJson(task.getResult().getValue());
                    callback.onSuccess(result);
                } else {
                    callback.onSuccess("services_not_found");
                }
            }
        });
    }

    public void updateService(ServiceTable service, String service_id, String client_id,
                              final RepoCallback callback) {
        database = FirebaseDatabase.getInstance(firebase_db_url)
                .getReference("service_table")
                .child("client_id")
                .child(client_id)
                .child("service_id")
                .child(service_id);

        database.setValue(service).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    callback.onSuccess("success");
                } else {
                    callback.onSuccess("failed");
                }
            }
        });
    }

    public void updateServiceActiveStatus(String service_id, String client_id, String status,
                                          final RepoCallback callback) {
        database = FirebaseDatabase.getInstance(firebase_db_url)
                .getReference("service_table")
                .child("client_id")
                .child(client_id)
                .child("service_id")
                .child(service_id)
                .child("status");

        database.setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    callback.onSuccess("success");
                } else {
                    callback.onSuccess("failed");
                }
            }
        });
    }

    public void updateServiceLocation(String service_id, String client_id, String location,
                                      final RepoCallback callback) {
        database = FirebaseDatabase.getInstance(firebase_db_url)
                .getReference("service_table")
                .child("client_id")
                .child(client_id)
                .child("service_id")
                .child(service_id)
                .child("location");

        database.setValue(location).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    callback.onSuccess("success");
                } else {
                    callback.onSuccess("failed");
                }
            }
        });
    }

    public void updateServiceCategory(String service_id, String client_id, String category,
                                      final RepoCallback callback) {
        database = FirebaseDatabase.getInstance(firebase_db_url)
                .getReference("service_table")
                .child("client_id")
                .child(client_id)
                .child("service_id")
                .child(service_id)
                .child("category");

        database.setValue(category).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    callback.onSuccess("success");
                } else {
                    callback.onSuccess("failed");
                }
            }
        });
    }

    public void updateServiceCustomer(String service_id, String client_id, String customer_id,
                                      final RepoCallback callback) {
        database = FirebaseDatabase.getInstance(firebase_db_url)
                .getReference("service_table")
                .child("client_id")
                .child(client_id)
                .child("service_id")
                .child(service_id)
                .child("customer_id");

        database.setValue(customer_id).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    callback.onSuccess("success");
                } else {
                    callback.onSuccess("failed");
                }
            }
        });
    }

    public void deleteService(String service_id, String client_id, final RepoCallback callback) {
        database = FirebaseDatabase.getInstance(firebase_db_url)
                .getReference("service_table")
                .child("client_id")
                .child(client_id)
                .child("service_id")
                .child(service_id);

        database.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    callback.onSuccess("success");
                } else {
                    callback.onSuccess("failed");
                }
            }
        });
    }

    public void deleteAllService(String client_id, final RepoCallback callback) {
        database = FirebaseDatabase.getInstance(firebase_db_url)
                .getReference("service_table")
                .child("client_id")
                .child(client_id);

        database.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    callback.onSuccess("success");
                } else {
                    callback.onSuccess("failed");
                }
            }
        });
    }


    //Order
    public void createOrder(OrderTable order, String customer_id, final RepoCallback callback) {
        database = FirebaseDatabase.getInstance(firebase_db_url)
                .getReference("order_table")
                .child(customer_id)
                .child("order_id")
                .child(primaryKeyGenerator("order_"));

        database.setValue(order).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    callback.onSuccess("success");
                } else {
                    callback.onSuccess("failed");
                }
            }
        });
    }

    public void getOrder(String order_id, String customer_id, final RepoCallback callback) {
        database = FirebaseDatabase.getInstance(firebase_db_url)
                .getReference("order_table")
                .child(customer_id)
                .child("order_id")
                .child(order_id);

        database.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful() && task.getResult().getValue() != null) {
                    String result = new Gson().toJson(task.getResult().getValue());
                    callback.onSuccess(result);
                } else {
                    callback.onSuccess("order_not_found");
                }
            }
        });
    }

    public void getAllOrders(final RepoCallback callback) {
        database = FirebaseDatabase.getInstance(firebase_db_url)
                .getReference("order_table");

        database.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful() && task.getResult().getValue() != null) {
                    String result = new Gson().toJson(task.getResult().getValue());
                    callback.onSuccess(result);
                } else {
                    callback.onSuccess("order_not_found");
                }
            }
        });
    }

    public void getCustomerOrders(String customer_id, final RepoCallback callback) {
        database = FirebaseDatabase.getInstance(firebase_db_url)
                .getReference("order_table")
                .child(customer_id);

        database.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful() && task.getResult().getValue() != null) {
                    String result = new Gson().toJson(task.getResult().getValue());
                    callback.onSuccess(result);
                } else {
                    callback.onSuccess("order_not_found");
                }
            }
        });
    }

    public void updateOrder(OrderTable order, String customer_id, String order_id,
                            final RepoCallback callback) {
        database = FirebaseDatabase.getInstance(firebase_db_url)
                .getReference("order_table")
                .child(customer_id)
                .child("order_id")
                .child(order_id);

        database.setValue(order).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    callback.onSuccess("success");
                } else {
                    callback.onSuccess("failed");
                }
            }
        });
    }

    public void updateOrderStatus(String customer_id, String order_id, String status,
                                  final RepoCallback callback) {
        database = FirebaseDatabase.getInstance(firebase_db_url)
                .getReference("order_table")
                .child(customer_id)
                .child("order_id")
                .child(order_id)
                .child("order_status");

        database.setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    callback.onSuccess("success");
                } else {
                    callback.onSuccess("failed");
                }
            }
        });
    }

    public void updateOrderRating(String customer_id, String order_id, String rating,
                                  final RepoCallback callback) {
        database = FirebaseDatabase.getInstance(firebase_db_url)
                .getReference("order_table")
                .child(customer_id)
                .child("order_id")
                .child(order_id)
                .child("rating");

        database.setValue(rating).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    callback.onSuccess("success");
                } else {
                    callback.onSuccess("failed");
                }
            }
        });
    }

    public void getSecure(final RepoCallback callback) {
        database = FirebaseDatabase.getInstance(firebase_db_url)
                .getReference("is_secure");

        database.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful() && task.getResult().getValue() != null) {
                    String result = new Gson().toJson(task.getResult().getValue());
                    callback.onSuccess(result);
                } else {
                    callback.onSuccess("false");
                }
            }
        });
    }

}
