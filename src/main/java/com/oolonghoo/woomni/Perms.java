package com.oolonghoo.woomni;

public final class Perms {
    
    private Perms() {
    }
    
    public static final String PREFIX = "wooomni";
    
    public static final String RELOAD = PREFIX + ".reload";
    public static final String HELP = PREFIX + ".help";
    
    public static final class Fly {
        private static final String BASE = PREFIX + ".fly";
        public static final String USE = BASE;
        public static final String OTHERS = BASE + ".others";
        public static final String SPEED = BASE + ".speed";
        public static final String BYPASS_DISABLED = BASE + ".bypass.disabled";
        
        private Fly() {
        }
    }
    
    public static final class God {
        private static final String BASE = PREFIX + ".god";
        public static final String USE = BASE;
        public static final String OTHERS = BASE + ".others";
        public static final String BYPASS_DISABLED = BASE + ".bypass.disabled";
        
        private God() {
        }
    }
}
