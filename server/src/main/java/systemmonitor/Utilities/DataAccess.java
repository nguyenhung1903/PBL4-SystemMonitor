package systemmonitor.Utilities;

import java.util.ArrayList;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import systemmonitor.Utilities.Classes.ProcessInfo;

class Status{
    public static boolean Warning = true;
    public static boolean Normal = false;
}

public class DataAccess {
    // private String key;

    JedisPool pool;
    static long lim = 100;

    public DataAccess() {
        pool = new JedisPool("localhost", 6379);
    }

    public void setStatus(String clientName, boolean status){
        try (Jedis jedis = pool.getResource()) {
            String key = "Client " + clientName + ":Status";
            jedis.del(key);
            jedis.set(key, Boolean.toString(status));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Boolean getStatus(String clientName){
        try (Jedis jedis = pool.getResource()) {
            String key = "Client " + clientName + ":Status";
            return Boolean.parseBoolean(jedis.get(key));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setProcessList(String clientName, ArrayList<ProcessInfo> processes) {
        try (Jedis jedis = pool.getResource()) {
            String key = "Client " + clientName + ":ProcessList";
            jedis.del(key);
            for (ProcessInfo process : processes) {
                jedis.rpush(key, process.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<ProcessInfo> getProcessList(String clientName) {
        ArrayList<ProcessInfo> list = new ArrayList<ProcessInfo>();

        try (Jedis jedis = pool.getResource()) {
            String key = "Client " + clientName + ":ProcessList";
            for (String s_process : jedis.lrange(key, 0, -1)) {
                list.add(new ProcessInfo(s_process));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }


        return list;
    }


    public ArrayList<Double> getCpuUsages(String clientName) {
        ArrayList<Double> list = new ArrayList<Double>();

        try (Jedis jedis = pool.getResource()) {
            String key = "Client " + clientName + ":CPU";

            for (String s_cpu : jedis.lrange(key, 0, lim)) {
                list.add(Double.parseDouble(s_cpu));
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Double getCurrentCpuUsage(String clientName) {
        try (Jedis jedis = pool.getResource()) {
            String key = "Client " + clientName + ":CPU";
            return Double.parseDouble(jedis.lindex(key, -1));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void addCpuUsage(String clientName, Double cpu) {
        try (Jedis jedis = pool.getResource()) {
            String key = "Client " + clientName + ":CPU";

            if (jedis.llen(key) >= lim)
                jedis.lpop(key);

            jedis.rpush(key, cpu.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Long> getMemoryUsages(String clientName) {
        try (Jedis jedis = pool.getResource()) {
            String key = "Client " + clientName + ":Memory";
            ArrayList<Long> list = new ArrayList<Long>();

            for (String s_mem : jedis.lrange(key, 0, lim)) {
                list.add(Long.parseLong(s_mem));
            }

            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public Long getCurrentMemoryUsage(String clientName) {
        try (Jedis jedis = pool.getResource()) {
            String key = "Client " + clientName + ":Memory";
            return Long.parseLong(jedis.lindex(key, -1));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public void addMemUsage(String clientName, Long mem) {
        try (Jedis jedis = pool.getResource()) {
            String key = "Client " + clientName + ":Memory";

            if (jedis.llen(key) >= lim)
                jedis.lpop(key);

            jedis.rpush(key, mem.toString());
        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    public String getIP(String clientName) {
        try (Jedis jedis = pool.getResource()) {
            String key = "Client " + clientName + ":IP";
            return jedis.get(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public void setIP(String clientName, String ip) {
        try (Jedis jedis = pool.getResource()) {
            String key = "Client " + clientName + ":IP";
            jedis.set(key, ip);
        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    public String getMAC(String clientName) {
        try (Jedis jedis = pool.getResource()) {
            String key = "Client " + clientName + ":MAC";
            return jedis.get(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public void setMAC(String clientName, String mac) {
        try (Jedis jedis = pool.getResource()) {
            String key = "Client " + clientName + ":MAC";
            jedis.set(key, mac);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getOSName(String clientName) {
        try (Jedis jedis = pool.getResource()) {
            String key = "Client " + clientName + ":OS";
            return jedis.get(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setOSName(String clientName, String osname) {
        try (Jedis jedis = pool.getResource()) {
            String key = "Client " + clientName + ":OS";
            jedis.set(key, osname);
        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    public Long getTotalMem(String clientName) {
        try (Jedis jedis = pool.getResource()) {
            String key = "Client " + clientName + ":TotalMem";
            return Long.parseLong(jedis.get(key));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setTotalMem(String clientName, Long totalMem) {
        try (Jedis jedis = pool.getResource()) {
            String key = "Client " + clientName + ":TotalMem";
            jedis.set(key, Long.toString(totalMem));
        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    public String getCPUModel(String clientName) {
        try (Jedis jedis = pool.getResource()) {
            String key = "Client " + clientName + ":CPUModel";
            return jedis.get(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public void setCPUModel(String clientName, String cpumodel) {
        try (Jedis jedis = pool.getResource()) {
            String key = "Client " + clientName + ":CPUModel";
            jedis.set(key, cpumodel);
        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    public Long getTotalStorage(String clientName) {
        try (Jedis jedis = pool.getResource()) {
            String key = "Client " + clientName + ":TotalStorage";
            return Long.parseLong(jedis.get(key));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public void setTotalStorage(String clientName, Long totalStorage) {
        try (Jedis jedis = pool.getResource()) {
            String key = "Client " + clientName + ":TotalStorage";
            jedis.set(key, Long.toString(totalStorage));
        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    public ArrayList<Double> getTrafficSend(String clientName) {
        try (Jedis jedis = pool.getResource()) {
            String key = "Client " + clientName + ":TrafficSend";
            ArrayList<Double> list = new ArrayList<Double>();

            for (String s_send : jedis.lrange(key, 0, lim)) {
                list.add(Double.parseDouble(s_send));
            }

            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public Double getCurrentTrafficSend(String clientName) {
        try (Jedis jedis = pool.getResource()) {
            String key = "Client " + clientName + ":TrafficSend";
            return Double.parseDouble(jedis.lindex(key, -1));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void addTrafficSend(String clientName, Double send) {
        try (Jedis jedis = pool.getResource()) {
            String key = "Client " + clientName + ":TrafficSend";

            if (jedis.llen(key) >= lim)
                jedis.lpop(key);

            jedis.rpush(key, send.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public ArrayList<Double> getTrafficReceived(String clientName) {
        try (Jedis jedis = pool.getResource()) {
            String key = "Client " + clientName + ":TrafficReceived";
            ArrayList<Double> list = new ArrayList<Double>();

            for (String s_received : jedis.lrange(key, 0, lim)) {
                list.add(Double.parseDouble(s_received));
            }

            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public Double getCurrentTrafficReceived(String clientName) {
        try (Jedis jedis = pool.getResource()) {
            String key = "Client " + clientName + ":TrafficReceived";
            return Double.parseDouble(jedis.lindex(key, -1));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void addTrafficReceived(String clientName, double received) {
        try (Jedis jedis = pool.getResource()) {
            String key = "Client " + clientName + ":TrafficReceived";
            if (jedis.llen(key) >= lim)
                jedis.lpop(key);

            jedis.rpush(key, Double.toString(received));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

//    public void close() {
//        if (jedis != null) {
//            jedis.close();
//        }
//    }
}
