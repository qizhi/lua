declare module cc {
    
    var SPRITE_INDEX_NOT_INITIALIZED;
    var TMX_ORIENTATION_HEX;
    var TMX_ORIENTATION_ISO;
    var TMX_ORIENTATION_ORTHO;
    var Z_COMPRESSION_BZIP2;
    var Z_COMPRESSION_GZIP;
    var Z_COMPRESSION_NONE;
    var Z_COMPRESSION_ZLIB;
    var BLEND_DST;
    var BLEND_SRC;
    var DIRECTOR_IOS_USE_BACKGROUND_THREAD;
    var DIRECTOR_MAC_THREAD;
    var DIRECTOR_STATS_INTERVAL;
    var ENABLE_BOX2_D_INTEGRATION;
    var ENABLE_DEPRECATED;
    var ENABLE_GL_STATE_CACHE;
    var ENABLE_PROFILERS;
    var ENABLE_STACKABLE_ACTIONS;
    var FIX_ARTIFACTS_BY_STRECHING_TEXEL;
    var GL_ALL;
    var LABELATLAS_DEBUG_DRAW;
    var LABELBMFONT_DEBUG_DRAW;
    var MAC_USE_DISPLAY_LINK_THREAD;
    var MAC_USE_MAIN_THREAD;
    var MAC_USE_OWN_THREAD;
    var NODE_RENDER_SUBPIXEL;
    var PVRMIPMAP_MAX;
    var SPRITEBATCHNODE_RENDER_SUBPIXEL;
    var SPRITE_DEBUG_DRAW;
    var TEXTURE_ATLAS_USE_TRIANGLE_STRIP;
    var TEXTURE_ATLAS_USE_VAO;
    var USE_L_A88_LABELS;
    var ACTION_TAG_INVALID;
    var DEVICE_MAC;
    var DEVICE_MAC_RETINA_DISPLAY;
    var DEVICEI_PAD;
    var DEVICEI_PAD_RETINA_DISPLAY;
    var DEVICEI_PHONE;
    var DEVICEI_PHONE5;
    var DEVICEI_PHONE5_RETINA_DISPLAY;
    var DEVICEI_PHONE_RETINA_DISPLAY;
    var DIRECTOR_PROJECTION2_D;
    var DIRECTOR_PROJECTION3_D;
    var DIRECTOR_PROJECTION_CUSTOM;
    var DIRECTOR_PROJECTION_DEFAULT;
    var FILE_UTILS_SEARCH_DIRECTORY_MODE;
    var FILE_UTILS_SEARCH_SUFFIX_MODE;
    var FLIPED_ALL;
    var FLIPPED_MASK;
    var IMAGE_FORMAT_JPEG;
    var IMAGE_FORMAT_PNG;
    var ITEM_SIZE;
    var LABEL_AUTOMATIC_WIDTH;
    var LINE_BREAK_MODE_CHARACTER_WRAP;
    var LINE_BREAK_MODE_CLIP;
    var LINE_BREAK_MODE_HEAD_TRUNCATION;
    var LINE_BREAK_MODE_MIDDLE_TRUNCATION;
    var LINE_BREAK_MODE_TAIL_TRUNCATION;
    var LINE_BREAK_MODE_WORD_WRAP;
    var MAC_VERSION_10_6;
    var MAC_VERSION_10_7;
    var MAC_VERSION_10_8;
    var MENU_HANDLER_PRIORITY:number;
    var MENU_STATE_TRACKING_TOUCH:number;
    var MENU_STATE_WAITING:number;
    var NODE_TAG_INVALID;
    var PARTICLE_DURATION_INFINITY;
    var PARTICLE_MODE_GRAVITY;
    var PARTICLE_MODE_RADIUS;
    var PARTICLE_START_RADIUS_EQUAL_TO_END_RADIUS;
    var PARTICLE_START_SIZE_EQUAL_TO_END_SIZE;
    var POSITION_TYPE_FREE;
    var POSITION_TYPE_GROUPED;
    var POSITION_TYPE_RELATIVE;
    var PRIORITY_NON_SYSTEM_MIN;
    var PRIORITY_SYSTEM;
    var PROGRESS_TIMER_TYPE_BAR:number;
    var PROGRESS_TIMER_TYPE_RADIAL:number;
    var REPEAT_FOREVER;
    var RESOLUTION_MAC;
    var RESOLUTION_MAC_RETINA_DISPLAY;
    var RESOLUTION_UNKNOWN;
    var TMX_TILE_DIAGONAL_FLAG;
    var TMX_TILE_HORIZONTAL_FLAG;
    var TMX_TILE_VERTICAL_FLAG;
    var TEXT_ALIGNMENT_CENTER:number;
    var TEXT_ALIGNMENT_LEFT:number;
    var TEXT_ALIGNMENT_RIGHT:number;
    var TEXTURE2_D_PIXEL_FORMAT_A8;
    var TEXTURE2_D_PIXEL_FORMAT_A_I88;
    var TEXTURE2_D_PIXEL_FORMAT_DEFAULT;
    var TEXTURE2_D_PIXEL_FORMAT_I8;
    var TEXTURE2_D_PIXEL_FORMAT_PVRTC2;
    var TEXTURE2_D_PIXEL_FORMAT_PVRTC4;
    var TEXTURE2_D_PIXEL_FORMAT_RG_B565;
    var TEXTURE2_D_PIXEL_FORMAT_RGB5_A1;
    var TEXTURE2_D_PIXEL_FORMAT_RG_B888;
    var TEXTURE2_D_PIXEL_FORMAT_RGB_A4444;
    var TEXTURE2_D_PIXEL_FORMAT_RGB_A8888;
    var TOUCHES_ALL_AT_ONCE;
    var TOUCHES_ONE_BY_ONE;
    var TRANSITION_ORIENTATION_DOWN_OVER;
    var TRANSITION_ORIENTATION_LEFT_OVER;
    var TRANSITION_ORIENTATION_RIGHT_OVER;
    var TRANSITION_ORIENTATION_UP_OVER;
    var UNIFORM_COS_TIME;
    var UNIFORM_MV_MATRIX;
    var UNIFORM_MVP_MATRIX;
    var UNIFORM_P_MATRIX;
    var UNIFORM_RANDOM01;
    var UNIFORM_SAMPLER;
    var UNIFORM_SIN_TIME;
    var UNIFORM_TIME;
    var UNIFORM_MAX;
    var VERTEX_ATTRIB_FLAG_COLOR;
    var VERTEX_ATTRIB_FLAG_NONE;
    var VERTEX_ATTRIB_FLAG_POS_COLOR_TEX;
    var VERTEX_ATTRIB_FLAG_POSITION;
    var VERTEX_ATTRIB_FLAG_TEX_COORDS;
    var VERTEX_ATTRIB_COLOR;
    var VERTEX_ATTRIB_MAX;
    var VERTEX_ATTRIB_POSITION;
    var VERTEX_ATTRIB_TEX_COORDS;
    var VERTICAL_TEXT_ALIGNMENT_BOTTOM:number;
    var VERTICAL_TEXT_ALIGNMENT_CENTER:number;
    var VERTICAL_TEXT_ALIGNMENT_TOP:number;
    var OS_VERSION_4_0;
    var OS_VERSION_4_0_1;
    var OS_VERSION_4_1;
    var OS_VERSION_4_2;
    var OS_VERSION_4_2_1;
    var OS_VERSION_4_3;
    var OS_VERSION_4_3_1;
    var OS_VERSION_4_3_2;
    var OS_VERSION_4_3_3;
    var OS_VERSION_4_3_4;
    var OS_VERSION_4_3_5;
    var OS_VERSION_5_0;
    var OS_VERSION_5_0_1;
    var OS_VERSION_5_1_0;
    var OS_VERSION_6_0_0;
    var ANIMATION_FRAME_DISPLAYED_NOTIFICATION;
    var CHIPMUNK_IMPORT;
    var ATTRIBUTE_NAME_COLOR;
    var ATTRIBUTE_NAME_POSITION;
    var ATTRIBUTE_NAME_TEX_COORD;
    var SHADER_POSITION_COLOR;
    var SHADER_POSITION_LENGTH_TEXURE_COLOR;
    var SHADER_POSITION_TEXTURE;
    var SHADER_POSITION_TEXTURE_A8_COLOR;
    var SHADER_POSITION_TEXTURE_COLOR;
    var SHADER_POSITION_TEXTURE_COLOR_ALPHA_TEST;
    var SHADER_POSITION_TEXTURE_U_COLOR;
    var SHADER_POSITION_U_COLOR;
    var UNIFORM_ALPHA_TEST_VALUE_S;
    var UNIFORM_COS_TIME_S;
    var UNIFORM_MV_MATRIX_S;
    var UNIFORM_MVP_MATRIX_S;
    var UNIFORM_P_MATRIX_S;
    var UNIFORM_RANDOM01_S;
    var UNIFORM_SAMPLER_S;
    var UNIFORM_SIN_TIME_S;
    var UNIFORM_TIME_S;

    var LANGUAGE_ENGLISH;
    var LANGUAGE_CHINESE;
    var LANGUAGE_FRENCH;
    var LANGUAGE_ITALIAN;
    var LANGUAGE_GERMAN;
    var LANGUAGE_SPANISH;
    var LANGUAGE_RUSSIAN;
    var LANGUAGE_KOREAN;
    var LANGUAGE_JAPANESE;
    var LANGUAGE_HUNGARIAN;
    var LANGUAGE_PORTUGUESE;
    var LANGUAGE_ARABIC;
    
    var DIRECTOR_PROJECTION_2D;
    var DIRECTOR_PROJECTION_3D;

    var TEXTURE_PIXELFORMAT_RGBA8888;
    var TEXTURE_PIXELFORMAT_RGB888;
    var TEXTURE_PIXELFORMAT_RGB565;
    var TEXTURE_PIXELFORMAT_A8;
    var TEXTURE_PIXELFORMAT_I8;
    var TEXTURE_PIXELFORMAT_AI88;
    var TEXTURE_PIXELFORMAT_RGBA4444;
    var TEXTURE_PIXELFORMAT_RGB5A1;
    var TEXTURE_PIXELFORMAT_PVRTC4;
    var TEXTURE_PIXELFORMAT_PVRTC4;
    var TEXTURE_PIXELFORMAT_DEFAULT;

    var TEXT_ALIGNMENT_LEFT;
    var TEXT_ALIGNMENT_CENTER;
    var TEXT_ALIGNMENT_RIGHT;

    var VERTICAL_TEXT_ALIGNMENT_TOP;
    var VERTICAL_TEXT_ALIGNMENT_CENTER;
    var VERTICAL_TEXT_ALIGNMENT_BOTTOM;

    var IMAGE_FORMAT_JPEG;
    var IMAGE_FORMAT_PNG;

    var PROGRESS_TIMER_TYPE_RADIAL;
    var PROGRESS_TIMER_TYPE_BAR;

    var PARTICLE_TYPE_FREE;
    var PARTICLE_TYPE_RELATIVE;
    var PARTICLE_TYPE_GROUPED;
    var PARTICLE_DURATION_INFINITY;
    var PARTICLE_MODE_GRAVITY;
    var PARTICLE_MODE_RADIUS;
    var PARTICLE_START_SIZE_EQUAL_TO_END_SIZE;
    var PARTICLE_START_RADIUS_EQUAL_TO_END_RADIUS;

    var TOUCH_ALL_AT_ONCE;
    var TOUCH_ONE_BY_ONE;

    var TMX_TILE_HORIZONTAL_FLAG;
    var TMX_TILE_VERTICAL_FLAG;
    var TMX_TILE_DIAGONAL_FLAG;

    var TRANSITION_ORIENTATION_LEFT_OVER;
    var TRANSITION_ORIENTATION_RIGHT_OVER;
    var TRANSITION_ORIENTATION_UP_OVER;
    var TRANSITION_ORIENTATION_DOWN_OVER;

    var RED;
    var GREEN;
    var BLUE;
    var BLACK;
    var WHITE;
    var YELLOW;

    var POINT_ZERO;

    // XXX: This definition is different than cocos2d-html5
    // var REPEAT_FOREVER 
    // We can't assign -1 to var REPEAT_FOREVER, since it will be a very big double value after
    // converting it to double by JS_ValueToNumber on android.
    // Then cast it to unsigned int, the value will be 0. The schedule will not be able to work.
    // I don't know why this ovar rs only on android.
    // So instead of passing -1 to it, I assign it with max value of unsigned int in c++.
    var REPEAT_FOREVER;

    var MENU_STATE_WAITING;
    var MENU_STATE_TRACKING_TOUCH;
    var MENU_HANDLER_PRIORITY;
    var DEFAULT_PADDING;

    var SCROLLVIEW_DIRECTION_NONE;
    var SCROLLVIEW_DIRECTION_HORIZONTAL;
    var SCROLLVIEW_DIRECTION_VERTICAL;
    var SCROLLVIEW_DIRECTION_BOTH;
    var TABLEVIEW_FILL_TOPDOWN;
    var TABLEVIEW_FILL_BOTTOMUP;


    /**
     * @constant
     * @type Number
     */
    var KEYBOARD_RETURNTYPE_DEFAULT;

    /**
     * @constant
     * @type Number
     */
    var KEYBOARD_RETURNTYPE_DONE;

    /**
     * @constant
     * @type Number
     */
    var KEYBOARD_RETURNTYPE_SEND;

    /**
     * @constant
     * @type Number
     */
    var KEYBOARD_RETURNTYPE_SEARCH;

    /**
     * @constant
     * @type Number
     */
    var KEYBOARD_RETURNTYPE_GO;

    /**
     * The EditBoxInputMode defines the type of text that the user is allowed * to enter.
     * @constant
     * @type Number
     */
    var EDITBOX_INPUT_MODE_ANY;

    /**
     * The user is allowed to enter an e-mail address.
     * @constant
     * @type Number
     */
    var EDITBOX_INPUT_MODE_EMAILADDR;

    /**
     * The user is allowed to enter an integer value.
     * @constant
     * @type Number
     */
    var EDITBOX_INPUT_MODE_NUMERIC;

    /**
     * The user is allowed to enter a phone number.
     * @constant
     * @type Number
     */
    var EDITBOX_INPUT_MODE_PHONENUMBER;

    /**
     * The user is allowed to enter a URL.
     * @constant
     * @type Number
     */
    var EDITBOX_INPUT_MODE_URL;

    /**
     * The user is allowed to enter a real number value.
     * This extends kEditBoxInputModeNumeric by allowing a decimal point.
     * @constant
     * @type Number
     */
    var EDITBOX_INPUT_MODE_DECIMAL;

    /**
     * The user is allowed to enter any text, except for line breaks.
     * @constant
     * @type Number
     */
    var EDITBOX_INPUT_MODE_SINGLELINE;

    /**
     * Indicates that the text entered is confidential data that should be
     * obscured whenever possible. This implies EDIT_BOX_INPUT_FLAG_SENSITIVE.
     * @constant
     * @type Number
     */
    var EDITBOX_INPUT_FLAG_PASSWORD;

    /**
     * Indicates that the text entered is sensitive data that the
     * implementation must never store into a dictionary or table for use
     * in predictive, auto-completing, or other avar lerated input schemes.
     * A credit card number is an example of sensitive data.
     * @constant
     * @type Number
     */
    var EDITBOX_INPUT_FLAG_SENSITIVE;

    /**
     * This flag is a hint to the implementation that during text editing,
     * the initial letter of each word should be capitalized.
     * @constant
     * @type Number
     */
    var EDITBOX_INPUT_FLAG_INITIAL_CAPS_WORD;

    /**
     * This flag is a hint to the implementation that during text editing,
     * the initial letter of each sentence should be capitalized.
     * @constant
     * @type Number
     */
    var EDITBOX_INPUT_FLAG_INITIAL_CAPS_SENTENCE;

    /**
     * Capitalize all characters automatically.
     * @constant
     * @type Number
     */
    var EDITBOX_INPUT_FLAG_INITIAL_CAPS_ALL_CHARACTERS;

    var log: any;
    //Color 3B
    export class c3b {
        r: number;
        g: number;
        b: number;
        constructor(r: number, g: number, b:number);
    }
    //Color 4B
    export class c4b {
        r: number;
        g: number;
        b: number;
        a: number;
        constructor(r: number, g: number, b: number, a:number);
    }
    //Color 4F
    export class c4f {
        r: number;
        g: number;
        b: number;
        a: number;
        constructor(r: number, g: number, b: number, a: number);
    }
    
    //Point, ccp
    export class p {
        x: number;
        y: number;
        constructor(x:number, y:number);
    }
    var pointEqualToPoint: (point1: p, point2: p) => boolean;

    export class size {
        width: number;
        height: number;
        constructor(w: number, h: number);
    }
    var sizeEqualToSize: (size1: size, size2: size) => boolean;

    export class rect {
        x: number;
        y: number;
        width: number;
        height: number;
        constructor(x: number, y: number, w: number, h: number);
    }
    var rectEqualToRect: (rect1: rect, rect2: rect) => boolean;
    var rectContainsRect: (rect1: rect, rect2: rect) => boolean;
    var rectGetMaxX: (rect: rect) => number;
    var rectGetMidX: (rect: rect) => number;
    var rectGetMinX: (rect: rect) => number;
    var rectGetMaxY: (rect: rect) => number;
    var rectGetMidY: (rect: rect) => number;
    var rectGetMinY: (rect: rect) => number;
    var rectContainsPoint: (rect: rect, point: p) => boolean;
    var rectIntersectsRect: (rectA: rect, rectB: rect) => boolean;
    var rectUnion: (rectA: rect, rectB: rect) => rect;
    var rectIntersection: (rectA: rect, rectB: rect) => rect;

    var ArrayGetIndexOfObject: (arr: Array<any>, findObj: any) => number;
    var ArrayContainsObject: (arr: Array<any>, findObj: any) => boolean;
    var ArrayRemoveObject: (arr: Array<any>, delObj: any) => void;

    var dump: (obj: any) => void;
    var dumpConfig: () => void;

    var associateWithNative: (jsobj: any, superclass_or_instanc: any) => void;
    var inherits: (childCtor: any, parentCtor: any) => void;
    var base: (me:any, opt_methodName:any, var_args:any) => void;

    export class Class {
        _super(): void;
    }
    export class GLProgram extends Class{
        addAttribute(attributeName: string, index: number): void;
        link(): boolean;
        use(): void;
        updateUniforms(): void;

        static create(vShaderFileName:string, fShaderFileName:string): GLProgram;
    }
    export class Node extends Class{
        cleanup(): void;
        
        visit(): void;
        transform(): void;

        getScriptHandler(): any;
        unregisterScriptHandler(): void;
        
        setShaderProgram(newShaderProgram: GLProgram): void;
        getShaderProgram(): GLProgram;

        convertToNodeSpace(p: p): p;//Point
        convertToWorldSpace(p: p): p;//Point
        convertToNodeSpaceAR(p: p): p;//Point
        convertToWorldSpaceAR(p: p): p;//Point

        convertTouchToNodeSpace(p: Touch): p;//Point
        convertTouchToNodeSpaceAR(p: Touch): p;//Point

        isIgnoreAnchorPointForPosition(): boolean;
        ignoreAnchorPointForPosition(v: boolean);

        getGrid(): GridBase;
        setGrid(grid: GridBase): void;

        setParent(v: Node): void;
        getParent(): Node;

        getRotation(): number;
        setRotation(v: number): void;

        getRotationX(): number;
        setRotationX(v: number): void;

        getRotationY(): number;
        setRotationY(v: number): void;

        getScale(): number;
        setScale(v: number): void;

        getScaleY(): number;
        setScaleY(v: number): void;

        getScaleX(): number;
        setScaleX(v: number): void;

        getSkewX(): number;
        setSkewX(v: number): void;

        setSkewY(v: number): void;
        getSkewY(): number;

        getVertexZ(): number;
        setVertexZ(v: number): void;

        getZOrder(): number;
        setZOrder(v: number): void;
        
        getTag(): number;
        setTag(v: number): void;

        setPosition(x:number, y:number): void;
        getPosition(): p;

        getPositionX(): number;
        setPositionX(v: number): void;

        getPositionY(): number;
        setPositionY(v: number): void;

        getOrderOfArrival(): number;
        setOrderOfArrival(v: number): void;

        isVisible(): boolean;
        setVisible(v: boolean): void;

        getAnchorPoint(): p;
        getAnchorPointInPoints(): p;
        setAnchorPoint(p: p): void;

        setContentSize(s: size): void;
        getContentSize(): size;

        getActionManager(): any;
        getScheduler(): any;

        isRunning(): boolean;

        getChildrenCount(): number;
        getChildren(): any[];
        
        getChildByTag(tag: number): Node;
        addChild(child:Node, zOrder?:number, tag?:number): void;
        removeFromParent(cleanup?: boolean): void;
        removeFromParentAndCleanup(cleanup?: boolean): void;
        removeChild(child: Node, cleanup?: boolean): void;

        removeChildByTag(tag: number, cleanup?: boolean): void;
        removeAllChildrenWithCleanup(cleanup?: boolean): void;
        removeAllChildren(cleanup?: boolean): void;

        reorderChild(child: Node, zOrder: number): void;
        sortAllChildren(): void;

        draw(ctx: any): void;

        onEnter(): void;
        onEnterTransitionDidFinish(): void;
        onExitTransitionDidStart(): void;
        onExit(): void;
        
        runAction(action: Action): void;
        stopAction(action: Action): void;
        stopAllActions(): void;
        stopActionByTag(tag: number): void;
        getActionByTag(tag: number): Action;
        numberOfRunningActions(): number;

        static create(...args: any[]): Node;
        static extend(prop: any): Node;
    }
    export class NodeRGBA extends Node {
        getOpacity(): number;
        setOpacity(opacity: number): void;

        getDisplayedOpacity(): number;
        updateDisplayedOpacity(v: number): void;

        getColor(): c3b;
        setColor(color3: c3b): void;

        getDisplayedColor(): c3b;
        updateDisplayedColor(v: c3b): void;

        isCascadeOpacityEnabled(): boolean;
        setCascadeOpacityEnabled(v: boolean): void;

        isCascadeColorEnabled(): boolean;
        setCascadeColorEnabled(v: boolean): void;

        isOpacityModifyRGB(): boolean;
        setOpacityModifyRGB(v: boolean): void;
    }
    export class GridBase {
        getGridSize(): size;
        setGridSize(v: size): void;
    }

    export class Sprite extends NodeRGBA {
        getTexture(): Texture2D;
        setTexture(v: Texture2D): void;
        
        setDisplayFrame(frame: SpriteFrame): void;
        getTextureRect(): rect;
        getOffsetPosition(): p;

        static createWithSpriteFrameName(spriteFrameName:string): Sprite;
        static createWithSpriteFrame(spriteFrame: SpriteFrame): Sprite;
        static create(...args: any[]): Sprite;
    }

    export class Texture2D extends Class {
        getName(): string;
    }
    export class SpriteFrame extends Class{
        getTexture(): Texture2D;
        setTexture(v: Texture2D): void;
    }

    export class SpriteFrameCache {
        static sharedSpriteFrameCache(): SpriteFrameCache;

        addSpriteFrames(plist: string, texture: string): void;
        addSpriteFrame(frame: SpriteFrame, frameName: string): void;

        getSpriteFrame(name: string): SpriteFrame;

        removeSpriteFrames(): void;
        removeSpriteFrameByName(name: string): void;
        removeSpriteFramesFromFile(plist: string): void;
    }

    export class TextureCache {
        dumpCachedTextureInfo(): string;
        removeAllTextures(): void;
        removeTexture(texture: Texture2D): void;
        removeTextureForKey(textureKeyName: string): void;
        addImage(image: string): void;

        textureForKey(textureKeyName: string): Texture2D;

        static sharedTextureCache(): TextureCache;
    }

    export class AnimationFrame extends Class {
        getSpriteFrame(): SpriteFrame;
        setSpriteFrame(v: SpriteFrame): void
    }
    export class Animation extends Class {
        addSpriteFrame(v: SpriteFrame): void;
        addSpriteFrameWithFile(v: string): void;
        addSpriteFrameWithTexture(texture: Texture2D, rect?: rect): void;
        
        getLoops(): number;
        setLoops(v: number): void;

        getDuration(): number;
        setDuration(v: number): void;

        getDelayPerUnit(): number;
        setDelayPerUnit(v: number): void;

        getTotalDelayUnits(): number;
    }
    export class LabelTTF extends Sprite{
        getString(): string;
        setString(text: string): void;

        getDuration(): number;
        setDuration(v: number): void;

        getHorizontalAlignment(): number;
        setHorizontalAlignment(v: number): void;

        getVerticalAlignment(): number;
        setVerticalAlignment(v: number): void;

        getDimensions(): size;
        setDimensions(v: size): void;

        getFontSize(): number;
        getFontSize(v: number): void;

        getFontName(): string;
        setFontName(v: string): void;

        enableShadow(shadowOffset: size, shadowOpacity: number, shadowBlur: number, mustUpdateTexture?: boolean): void;
        disableShadow(mustUpdateTexture?: boolean): void;

        enableStroke(strokeColor: c3b, strokeSize: number, mustUpdateTexture?: boolean): void;
        disableStroke(mustUpdateTexture?: boolean): void;

        setFontFillColor(tintColor: c3b, mustUpdateTexture?: boolean): void;

        static create(label:string, fontName?:String, fontSize?:number, dimensions?:size, alignment?:number): LabelTTF;
    }
    export class SpriteBatchNode extends Node {

    }
    export class LabelBMFont extends SpriteBatchNode {
        getOpacity(): number;
        setOpacity(opacity: number): void;

        getColor(): c3b;
        setColor(color3: c3b): void;

        createFontChars(): void;
        updateLabel(): void;

        getString(): string;
        setString(text: string): void;

        getFntFile(): string;
        setFntFile(v: string): void;

        setLineBreakWithoutSpace(breakWithoutSpace: boolean): void;
        setAlignment(v: number): void;
        setWidth(v: number): void;

        purgeCachedData(): void;

        static create(str:string, fntFile:string, width?:number, alignment?:number, imageOffset?:size): LabelBMFont;
    }
    export class Touch {
        getPreviousLocationInView(): p;
        getLocation(): p;
        getDelta(): p;
        getStartLocationInView(): p;
        getStartLocation(): p;
        getID(): number;
        getLocationInView(): p;
        getPreviousLocation(): p;
        setTouchInfo(id:number, x:number, y:number): void;
    }
    export class Set {
        count(): number;
        addObject(obj: any): void;
        removeAllObjects(): void;
        removeObject(obj: any): void;
        containsObject(obj: any): boolean;
        anyObject(): any;
        static create(): Set;
    }
    export class Layer extends Node {
        setTouchEnabled(t: boolean): void;
        isTouchEnabled(): boolean;

        setAccelerometerEnabled(t: boolean): void;
        isAccelerometerEnabled(): boolean;

        setKeypadEnabled(t: boolean): void;
        isKeypadEnabled(): boolean;

        setTouchPriority(): number;
        getTouchPriority(v: number): void;

        getTouchMode(): number;
        setTouchMode(v: number): void;

        keyBackClicked(): void;

        onTouchBegan(touch: Touch, event: any): boolean;
        onTouchMoved(touch: Touch, event: any): void;
        onTouchEnded(touch: Touch, event: any): void;
        onTouchCancelled(touch: Touch, event: any): void;

        static create(...args:any[]): Layer;
        static extend(prop: any): Layer;
    }

    export class LayerColor extends Layer {
        changeWidthAndHeight(w: number, h: number): void;
        changeWidth(w: number): void;
        changeHeight(h: number): void;

        getOpacity(): number;
        setOpacity(v: number): void;

        getColor(): c3b;
        setColor(v: c3b): void;

        static create(color?: c4b, width?: number, height?: number): LayerColor;
        static extend(prop: any): LayerColor;
    }

    export class Scene extends Node{
        static create(): Scene;
        static extend(prop: any): Scene;
    }
    export class ParticleSystem extends Node {
        static create(pListFile: string): ParticleSystem;
        setAutoRemoveOnFinish(t: boolean): void;
        isAutoRemoveOnFinish(): boolean;
    }
    export class Director {
        purgeCachedData(): void;
        end(): void;
        pause(): void;
        resume(): void;

        isPaused(): boolean;

        popScene(): void;
        pushScene(scene: Scene): void;
        replaceScene(scene: Scene): void;
        runWithScene(scene: Scene): void;
        popToRootScene(): void;

        getRunningScene(): Scene;

        setContentScaleFactor(t: number): void;
        getContentScaleFactor(): number;

        setDisplayStats(t: boolean): void;
        isDisplayStats(): boolean;

        getVisibleSize(): number;
        getWinSize(): size;

        setDepthTest(on: boolean): void;

        static sharedDirector(): Director;
        static getInstance(): Director;
    }
    export class Application extends Class{
        getTargetPlatform(): string;
        getCurrentLanguage(): string;
        static sharedApplication(): Application;
    }
    export class Action extends Class{
        startWithTarget(t: Node): void;
        setOriginalTarget(t: Node): void;
        setTarget(t: Node): void;
        getTarget(): Node;
        getOriginalTarget(): Node;

        stop(): void;
        update(t: number): void;
        step(t: number): void;

        setTag(t: number): void;
        getTag(): number;

        isDone(): boolean;
    }
    export class FiniteTimeAction extends Action {
    }
    export class ActionInterval extends FiniteTimeAction {
        setAmplitudeRate(t: number): void;
        getAmplitudeRate(): number;
        reverse(): ActionInterval;
    }
    export class Sequence extends ActionInterval {
        static create(...args:any[]): Sequence;
    }
    export class Spawn extends ActionInterval {
        static create(...args: any[]): Sequence;
    }
    export class Repeat extends ActionInterval {
        static create(action: FiniteTimeAction, times:number): Repeat;
    }
    export class RepeatForever extends ActionInterval {
        static create(action: FiniteTimeAction): RepeatForever;
    }
    export class RotateTo extends ActionInterval {
        static create(duration:number, deltaAngleX:number, deltaAngleY?:number): RotateTo;
    }
    export class RotateBy extends ActionInterval {
        static create(duration: number, deltaAngleX: number, deltaAngleY?: number): RotateBy;
    }
    export class MoveTo extends ActionInterval {
        static create(duration: number, position:p): MoveTo;
    }
    export class MoveBy extends ActionInterval {
        static create(duration: number, position: p): MoveBy;
    }
    export class SkewTo extends ActionInterval {
        static create(duration: number, sx: number, sy: number): SkewTo;
    }
    export class SkewBy extends ActionInterval {
        static create(duration: number, sx: number, sy: number): SkewBy;
    }
    export class ScaleTo extends ActionInterval {
        static create(duration: number, sx: number, sy: number): ScaleTo;
    }
    export class ScaleBy extends ActionInterval {
        static create(duration: number, sx: number, sy: number): ScaleBy;
    }
    export class Blink extends ActionInterval {
        static create(duration: number, blinks: number): Blink;
    }
    export class FadeIn extends ActionInterval {
        static create(duration: number): Blink;
    }
    export class FadeOut extends ActionInterval {
        static create(duration: number): FadeOut;
    }
    export class FadeTo extends ActionInterval {
        static create(duration: number, opacity: number): FadeTo;
    }
    export class TintTo extends ActionInterval {
        static create(duration: number, red:number, green:number, blue:number): TintTo;
    }
    export class TintBy extends ActionInterval {
        static create(duration: number, red: number, green: number, blue: number): TintBy;
    }

    export class JumpBy extends ActionInterval {
        static create(duration:number, position:p, height:number, jumps:number): JumpBy;
    }
    export class JumpTo extends ActionInterval {
        static create(duration: number, position: p, height: number, jumps: number): JumpTo;
    }
    export class BezierBy extends ActionInterval {
        static create(duration: number, c:any): BezierBy;
    }
    export class BezierTo extends ActionInterval {
        static create(duration: number, c: any): BezierTo;
    }
    export class DelayTime extends ActionInterval {
        static create(duration: number): DelayTime;
    }
    export class Animate extends ActionInterval {
        setAnimation(v: Animation): void;
        getAnimation(): Animation;
        static create(animation: Animation): Animate;
    }
    export class TargetedAction extends ActionInterval {
        setForcedTarget(v: Node):void;
        getForcedTarget(): Node;
        static create(target: Node, action: FiniteTimeAction): TargetedAction;
    }
    export class Speed extends Action {
        setSpeed(v: number): void;
        getSpeed(): number;
        static create(action: ActionInterval, speed: number): Speed;
    }
    export class Follow extends Action {
        setBoudarySet(v: boolean): void;
        isBoundarySet(): boolean;
        static create(followedNode: Node, rect: rect): Follow;
    }
    export class ActionInstant extends FiniteTimeAction {
    }
    
    export class Show extends ActionInstant {
        static create(): Show;
    }
    export class Hide extends ActionInstant {
        static create(): Hide;
    }
    export class ToggleVisibility extends ActionInstant {
        static create(): ToggleVisibility;
    }
    export class FlipX extends ActionInstant {
        static create(x:boolean): FlipX;
    }
    export class FlipY extends ActionInstant {
        static create(x: boolean): FlipY;
    }
    export class Place extends ActionInstant {
        static create(pos: p): Place;
    }
    export class CallFunc extends ActionInstant {
        static create(selector:(nodeExecutingAction?:Node, value?:any)=>void, selectorTarget:Node, data?:any): Place;
    }
    export class ActionEase extends ActionInterval {
    }
    
    export class EaseIn extends ActionEase {
        static create(action: ActionInterval, rate: number): ActionEase;
    }
    export class EaseOut extends ActionEase {
        static create(action: ActionInterval, rate: number): ActionEase;
    }
    export class EaseInOut extends ActionEase {
        static create(action: ActionInterval, rate: number): ActionEase;
    }

    export class EaseExponentialIn extends ActionEase {
        static create(action: ActionInterval, rate: number): ActionEase;
    }
    export class EaseExponentialOut extends ActionEase {
        static create(action: ActionInterval, rate: number): ActionEase;
    }
    export class EaseExponentialInOut extends ActionEase {
        static create(action: ActionInterval, rate: number): ActionEase;
    }

    export class EaseSineIn extends ActionEase {
        static create(action: ActionInterval, rate: number): ActionEase;
    }
    export class EaseSineOut extends ActionEase {
        static create(action: ActionInterval, rate: number): ActionEase;
    }
    export class EaseSineInOut extends ActionEase {
        static create(action: ActionInterval, rate: number): ActionEase;
    }

    export class EaseElastic extends ActionEase {
        static create(action: ActionInterval, rate: number): ActionEase;
    }
    export class EaseElasticOut extends ActionEase {
        static create(action: ActionInterval, rate: number): ActionEase;
    }
    export class EaseElasticInOut extends ActionEase {
        static create(action: ActionInterval, rate: number): ActionEase;
    }

    export class EaseBounceIn extends ActionEase {
        static create(action: ActionInterval, rate: number): ActionEase;
    }
    export class EaseBounceOut extends ActionEase {
        static create(action: ActionInterval, rate: number): ActionEase;
    }
    export class EaseBounceInOut extends ActionEase {
        static create(action: ActionInterval, rate: number): ActionEase;
    }
    
    export class EaseBackIn extends ActionEase {
        static create(action: ActionInterval, rate: number): ActionEase;
    }
    export class EaseBackOut extends ActionEase {
        static create(action: ActionInterval, rate: number): ActionEase;
    }
    export class EaseBackInOut extends ActionEase {
        static create(action: ActionInterval, rate: number): ActionEase;
    }
    //var to = cc.ProgressTo.create(2, 100);
    export class ProgressTo extends ActionInterval {
        static create(duration:number, percent:number): ProgressTo;
    }
    export class ProgressFromTo extends ActionInterval {
        static create(duration: number, fromPercentage: number, toPercentage: number): ProgressFromTo;
    }
    export class ActionTween extends ActionInterval {
        static create(duration:number, key:string, from:number, to:number): ActionTween;
    }
    export class PageTurn3D extends ActionInterval {
        static create(duration: number, gridSize: size): PageTurn3D;
    }
    export class FlipX3D extends ActionInterval {
        static create(duration: number): FlipX3D;
    }
    export class FlipY3D extends ActionInterval {
        static create(duration: number): FlipY3D;
    }
    export class Lens3D extends ActionInterval {
        static create(duration:number, gridSize:size, position:p, radius:number): FlipY3D;
    }
    //var action1 = cc.CardinalSplineTo.create(3, array, 0);points array of control points
    export class CardinalSplineTo extends ActionInterval {
        static create(duration: number, points: p[], tension: number): CardinalSplineTo;
    }
    export class CardinalSplineBy extends ActionInterval {
        static create(duration: number, points: p[], tension: number): CardinalSplineBy;
    }
    export class CatmullRomTo extends ActionInterval {
        static create(dt:number, points:p[]): CatmullRomTo
    }
    export class CatmullRomBy extends ActionInterval {
        static create(dt: number, points: p[]): CatmullRomTo
    }
    export class Menu extends Layer{
        isEnabled(): boolean;
        setEnabled(v: boolean): void;

        getOpacity(): number;
        setOpacity(v: number): void;

        getColor(): c3b;
        setColor(v: c3b): void;

        alignItemsVertically(): void;
        alignItemsHorizontally(): void;
        alignItemsVerticallyWithPadding(v: number): void;
        alignItemsHorizontallyWithPadding(v: number): void;

        static create(...args:any[]): Menu;
    }
    export class MenuItem extends Node {
        isSelected(): boolean;
        activate(): void;

        selected(): void;
        unselected(): void;

        isEnabled(): boolean;
        setEnabled(v: boolean): void;

        rect(): rect;
    }
    export class MenuItemLabel extends MenuItem {
        getLabel(): Node;
        setLabel(v: Node): void;

        getOpacity(): number;
        setOpacity(v: number): void;

        getColor(): c3b;
        setColor(v: c3b): void;

        getDisabledColor(): c3b;
        setDisabledColor(v: c3b): void;

        static create(label: Node, selector?: (sender:Node) => void , target?: Node):MenuItemLabel;
    }
    export class MenuItemFont extends MenuItem {
        setFontSize(s: number): void;
        fontSize(): number;

        fontName(): string;
        setFontName(v: string): void;
        static create(str: string, selector?: (sender: Node) => void , target?: Node): MenuItemFont;
    }
    export class MenuItemSprite extends MenuItem {
        getNormalImage(): Node;
        setNormalImage(v: Node): void;

        getSelectedImage(): Node;
        setSelectedImage(v: Node): void;

        getDisabledImage(): Node;
        setDisabledImage(v: Node): void;
        
        getOpacity(): number;
        setOpacity(v: number): void;

        getColor(): c3b;
        setColor(v: c3b): void;

        static create(normalImage: Node, selectedImage: Node, disabledImage: Node, selector?: (sender: Node) => void , target?: Node): MenuItemSprite;
    }
    export class MenuItemToggle extends MenuItem {
        getOpacity(): number;
        setOpacity(v: number): void;
        getColor(): c3b;
        setColor(v: c3b): void;
        getSelectedIndex(): number;
        setSelectedIndex(v: number): void;
        static create(target: Node, selector: (sender: Node) => void , on: MenuItem, off: MenuItem): MenuItemToggle;
    }
    export class ProgressTimer extends NodeRGBA {
        getType(): number;
        setType(v: number): void;

        getPercentage(): number;
        setPercentage(v: number): void;

        getBarChangeRate(): p;
        setBarChangeRate(v: p): void;

        setMidpoint(): p;
        setMidpoint(v: p): void;

        isReverseDirection(): boolean;

        static create(sprite:string): ProgressTimer;
    }
    export class Loader {
        static preload(resources:any[], selector?:any, target?:any): Loader;
    }
    export class FileUtils {
        //jsp only
        isFileExist(f: string): boolean;
        getFileDataFromZip(pszZipFilePath: string, fileName: string, size: number): string;
        static sharedFileUtils(): FileUtils;

        //common
        fullPathForFilename(name: string): string;
        fullPathFromRelativeFile(name: string, relativeFile: string): string;
        setSearchResolutionsOrder(searchResolutionsOrder: string): void;
        getSearchResolutionsOrder(): string;

        setSearchPaths(path: string): void;
        addSearchPath(path: string): void;
        isAbsolutePath(path: string): boolean;
        static getInstance(): FileUtils;
    }
    export class AudioEngine {
        static sharedEngine(): AudioEngine;

        preloadBackgroundMusic(v: string): void;
        preloadEffect(v: string): void;
        unloadEffect(v: string): void;

        playMusic(path: string, loop: boolean): void;
        stopMusic(releaseData?: boolean): void;

        pauseBackgroundMusic(): void;
        resumeBackgroundMusic(): void;
        isBackgroundMusicPlaying(): boolean;

        stopAllEffects(): void;
        pauseAllEffects(): void;

        resumeAllEffects(): void;

        stopEffect(v: number): void;
        pauseEffect(v: number): void;
        resumeEffect(v: number): void;

        getBackgroundMusicVolume(): number;
        setBackgroundMusicVolume(v: number): void;

        getEffectsVolume(): number;
        setEffectsVolume(v: number): void;
    }
    export class Scale9Sprite extends NodeRGBA {
        static create(file: string, rect?: rect, capInsets?: rect): Scale9Sprite;
        static createWithSpriteFrame(spriteFrame: SpriteFrame, capInsets?: rect): Scale9Sprite;
        static createWithSpriteFrameName(spriteFrameName:string, capInsets?:rect): Scale9Sprite;
    }
    export class Control extends Layer {
        isEnabled(): boolean;
        setEnabled(v: boolean): void;

        setHighlighted(v: boolean): void;
        isHighlighted(): boolean;
        
        getOpacity(): number;
        setOpacity(v: number): void;

        getColor(): c3b;
        setColor(v: c3b): void;

        getState(): number;

        setSelected(v: boolean): void;
        getTouchLocation(touch: Touch): p;
        isTouchInside(touch: Touch): boolean;
    }
    export class ControlButton extends Control {
        getAdjustBackgroundImage(): Scale9Sprite;
        setAdjustBackgroundImage(v: Scale9Sprite): void;

        getZoomOnTouchDown(): boolean;
        setZoomOnTouchDown(v: boolean): void;

        getPreferredSize(): size;
        setPreferredSize(v: size): void;

        getLabelAnchorPoint(): p;
        setLabelAnchorPoint(v: p): void;

        getIsPushed(): boolean;

        getVerticalMargin(): number;
        setMargins(marginH: number, marginV: number): void;

        getTitleForState(s: number): string;
        setTitleForState(title: string, state: number): void;

        getTitleColorForState(s: number): c3b;
        setTitleColorForState(color: c3b, s: number): void;

        getBackgroundSpriteForState(s: number): Scale9Sprite;
        setBackgroundSpriteForState(sprite: Scale9Sprite, state: number): void;

        static create(label:LabelTTF, backgroundSprite:Scale9Sprite): ControlButton;
    }
    export interface EditBoxDelegate {
        editBoxEditingDidBegin(sender: EditBox): void;
        editBoxEditingDidEnd(sender: EditBox): void;
        editBoxTextChanged(sender: EditBox, text:string): void;
        editBoxReturn(sender: EditBox): void;
    }
    export class EditBox extends ControlButton {
        setFont(fontName: string, fontSize: number): void;
        setText(text: string): void;
        getText(): string;

        setFontColor(color: c3b): void;

        setDelegate(d: EditBoxDelegate): void;
        getDelegate(): EditBoxDelegate;

        getMaxLength(): number;
        setMaxLength(v: number): void;

        setPlaceHolder(t: string): void;
        setPlaceholderFontColor(color: c3b): void;

        setInputFlag(f: number): void;
        setReturnType(v: number): void;

        getEffectsVolume(): number;
        setEffectsVolume(v: number): void;

        static create(...args:any[]): EditBox;
        //static create(size: rect, normal9SpriteBg: Scale9Sprite, press9SpriteBg?: Scale9Sprite, disabled9SpriteBg?: Scale9Sprite): EditBox;
    }
    export interface ScrollViewDelegate{
        scrollViewDidScroll(v: Node): void;
        scrollViewDidZoom(v: Node): void;
    }
    export class ScrollView extends Layer {
        getContentOffset(): p;
        setContentOffset(offset: p, animated?: boolean): void;
        setContentOffsetInDuration(offset: p, dt: number): void;
        setZoomScale(scale: number, animated?: boolean): void;
        getZoomScale(): number;
        setZoomScaleInDuration(s: number, dt: number): void;
        isDragging(): boolean;
        isClippingToBounds(): boolean;

        getViewSize(): size;
        setViewSize(s: size): void;

        getContainer(): Node;
        setContainer(v: Node): void;

        getDirection(): number;
        setDirection(v: number): void;

        setTouchEnabled(v: boolean): void;

        //static create(size: size): ScrollView;
    }
    export interface TableViewDelegate {
        scrollViewDidScroll(v: Node): void;
        scrollViewDidZoom(v: Node): void;
        tableCellTouched(t: TableView, cell: TableViewCell): void;
    }
    export interface TableViewDataSource {
        cellSizeForTable(t: TableView): cc.size;
        tableCellAtIndex(t: TableView, idx: number): TableViewCell;
        numberOfCellsInTableView(t: TableView): number;
    }
    export class TableViewCell extends Node {
        getObjectID(): number;
        setObjectID(v: number): void;

        getIdx(): number;
        getIdx(v: number): void;

        reset(): void;
    }
    export class TableView extends ScrollView {
        reloadData(): void;

        getDataSource(): TableViewDataSource;
        setDataSource(v: TableViewDataSource): void;

        getDelegate(): TableViewDelegate;
        setDelegate(v: TableViewDelegate, isDirectCall?: boolean): void;

        getVerticalFillOrder(): number;
        setVerticalFillOrder(v: number): void;

        updateCellAtIndex(idx: number): void;
        insertCellAtIndex(idx: number): void;
        removeCellAtIndex(idx: number): void;

        dequeueCell(): TableViewCell;
        cellAtIndex(idx: number): TableViewCell;

        static create(dataSource: TableViewDataSource, size:size): TableView;
    }
}
