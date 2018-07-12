package com.dfire.platform.alchemy.connectors.redis;

public class Sentinel{

        private String sentinels;

        private String master;

        public String getSentinels() {
            return sentinels;
        }

        public void setSentinels(String sentinels) {
            this.sentinels = sentinels;
        }

        public String getMaster() {
            return master;
        }

        public void setMaster(String master) {
            this.master = master;
        }
}