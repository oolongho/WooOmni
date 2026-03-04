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
    
    public static final class Vanish {
        private static final String BASE = PREFIX + ".vanish";
        public static final String USE = BASE;
        public static final String OTHERS = BASE + ".others";
        public static final String LIST = BASE + ".list";
        public static final String SEE = BASE + ".see";
        public static final String AUTOJOIN = BASE + ".autojoin";
        public static final String EDIT = BASE + ".edit";
        public static final String EDIT_OTHERS = BASE + ".edit.other";
        public static final String BYPASS_DISABLED = BASE + ".bypass.disabled";
        
        private Vanish() {
        }
    }
}
