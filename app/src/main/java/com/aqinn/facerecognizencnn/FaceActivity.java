package com.aqinn.facerecognizencnn;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.aqinn.facerecognizencnn.utils.CameraUtils;
import com.aqinn.facerecognizencnn.utils.CommonUtils;
import com.aqinn.facerecognizencnn.utils.Utils;
import com.aqinn.facerecognizencnn.view.AutoFitTextureView;
import com.aqinn.facerecognizencnn.view.DrawView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Aqinn
 * @date 2021/1/6 3:56 PM
 */
public class FaceActivity extends AppCompatActivity {

    private static final String TAG = "FaceActivity";

    private CameraCaptureSession mCaptureSession;
    private CameraDevice mCameraDevice;

    private HandlerThread mCaptureThread;
    private Handler mCaptureHandler;
    private HandlerThread mInferThread;
    private Handler mInferHandler;

    private ImageReader mImageReader;
    private boolean isFont = true;
    private Size mPreviewSize;
    private boolean mCapturing;
    private AutoFitTextureView mTextureView;  // 根据比例设置长宽的 TextureView
    //    private DrawView mDrawView;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private TextView tv_res;
    private Button bt_switch_camera;

    private final Object lock = new Object();  // 同步变量
    private boolean runClassifier = false;
    private ArrayList<String> classNames;
    static final boolean USE_GPU = false;

    private FaceRecognize mFaceRecognize;

    private float[] zzfFeature = new float[]{0.04015081f, 0.1812439f, 0.07658172f, 0.08661857f, 0.058135048f, 0.037191577f, 0.16201895f, 0.016380575f, 0.012543588f, -0.002694758f, 0.0016245797f, 0.024653504f, -0.024250748f, 0.03579985f, 0.054862283f, 0.033140656f, 0.060497187f, 0.0014048337f, 0.005525556f, 0.07473132f, 0.01593628f, -0.05520068f, 0.01024227f, 0.041424256f, 0.18087927f, -0.09261448f, -0.062208153f, 0.07450808f, 0.0853852f, 0.09790118f, -0.10316423f, -0.055556662f, -0.09425919f, 0.077613525f, 0.066494554f, -0.01740248f, 0.10049373f, -0.013840362f, -0.0056327106f, 1.5742084E-4f, 0.04658679f, 0.100569464f, -0.059280537f, 0.13061312f, -0.014313153f, 0.08953486f, 0.13817321f, -0.13607316f, -0.10813432f, -0.0060110455f, 0.10122166f, -0.006059737f, 0.03780583f, 0.016721835f, 0.123862244f, 0.04822603f, 0.016392479f, 0.0033125177f, -0.035765886f, 0.06360551f, 0.05839298f, 0.08212528f, 0.0016569388f, 0.06344758f, -0.03878882f, 0.07412493f, 0.008550011f, 0.013287695f, 0.025248608f, 0.04881134f, 0.080777004f, -0.16804358f, -0.11072618f, -0.0063142036f, 0.053497676f, 0.05111808f, -0.058108322f, 0.07987638f, 0.073728226f, 0.08542078f, 0.1395794f, 0.0202881f, 0.14863975f, -0.07472989f, 0.0029470953f, -0.007305622f, 0.07767666f, -0.056506798f, -0.09205013f, -0.18045709f, -0.016988872f, 1.8211853E-4f, 0.018742494f, -0.065147676f, 0.11410066f, -0.30399737f, -0.009116298f, 0.07032801f, -0.028826125f, 0.09006001f, 0.03249428f, -0.13217592f, 0.040678803f, -0.06372539f, -0.17237632f, 0.09422212f, 0.27660686f, 0.0588225f, -0.11730184f, 0.22920755f, -0.0063681877f, 0.060868382f, -0.06994681f, -0.050324954f, 0.028927645f, 0.12796496f, 0.0062486674f, 0.10239482f, -0.02916401f, -0.10661604f, 0.083037205f, 0.18232061f, 0.09390636f, 0.05940848f, 0.120802455f, 0.06320909f, 0.0922718f, -0.03609907f};
    private float[] zstFeature = new float[]{0.05970229f, -0.11959532f, -0.038781136f, 0.0773658f, 0.13486528f, -0.09360165f, -0.11554393f, 0.08390642f, -0.04670055f, 0.071562015f, 0.026826797f, -0.041955393f, -0.14587332f, -0.024605982f, -0.029576458f, -0.08087686f, -0.08142083f, 0.1004409f, 0.101697765f, 0.081570394f, -0.16950384f, 0.031134805f, -0.05547683f, -0.11220648f, 0.15609884f, -0.059951518f, 0.046780962f, 0.026593586f, 0.089081585f, 0.15091568f, -0.022692647f, -0.1014284f, 0.005658591f, 0.0099422475f, 0.09418518f, -0.025132965f, 0.10064309f, -0.2451703f, -0.11801287f, -0.023411833f, -0.082333155f, 0.026736151f, 0.110778056f, 0.0038314948f, 0.04386288f, 0.018075211f, -0.16478735f, 0.0043419823f, 0.077125065f, 0.079857245f, 0.0077643176f, 0.023152817f, 0.009500812f, -0.015327244f, -0.004322527f, -0.0068095685f, 0.030966638f, 0.007203795f, 0.08733561f, -0.16971017f, 0.12141284f, 0.1894848f, -0.16688333f, -0.0060075307f, 0.042282507f, -0.06247832f, 0.1596926f, -0.039924562f, 0.07596387f, 0.014761612f, -0.053202167f, -0.03690652f, 0.040757883f, 0.10480203f, 0.0898082f, 0.15842332f, -0.20646192f, -0.053363744f, 0.061452765f, 0.030003365f, 0.07953055f, -0.02311499f, 0.07760245f, 0.042822354f, -0.059343964f, -0.11005136f, 0.027201107f, -0.09968363f, -0.08426719f, -0.012694852f, -0.015127365f, 0.17570591f, -0.071121f, 0.058934655f, -0.056008987f, -0.19438091f, 0.06709881f, -0.13260871f, -0.08624404f, -0.054292604f, 0.08818124f, 0.033784255f, -0.041354295f, -0.017817464f, -0.086586006f, 0.13483694f, -0.004586582f, 0.066885315f, -0.017022707f, 0.06589148f, 0.18699753f, 0.03386011f, 0.005108937f, -0.15299565f, -0.062682234f, -0.049523786f, 0.080202214f, -0.010959659f, -0.0437852f, -0.11220157f, -0.08622056f, 0.049290136f, -0.07162684f, -0.062435504f, 0.09186652f, -0.058351822f, -0.014451836f, 0.0029467188f};
    private float[] zljFeature = new float[]{0.061676696f, 0.05240627f, -0.062351223f, -0.07415391f, -0.07505479f, -0.03576784f, 0.088858016f, -0.02007876f, 0.11133704f, -0.21377192f, 0.18313664f, -0.017251402f, 0.020222569f, 0.035712034f, -2.3663926E-4f, 0.009947484f, 0.0869742f, 0.0046895877f, -0.023310043f, 0.13587978f, -0.019196384f, -0.11795243f, -0.01858679f, 0.17596143f, 0.021916708f, -0.25416085f, 0.11678274f, -0.059217345f, -0.11187548f, 0.041371312f, 0.023463966f, -0.06965982f, -0.011366871f, 0.11089901f, 0.031062927f, 0.04102209f, 0.0155128995f, -0.05404919f, -0.037707284f, 0.023087356f, 0.047371175f, 0.04976896f, 0.021348426f, -0.050375063f, 0.08497113f, -0.2398282f, -0.07314179f, -0.12730283f, 0.001398627f, -0.09048205f, -0.12082522f, -0.02611939f, -0.08717261f, -0.026668642f, 0.11646825f, -0.04944291f, 0.10519671f, -0.11290832f, 0.06840749f, -0.005607028f, -0.037758622f, -0.005825825f, 0.02863935f, 0.03797917f, 0.12254987f, 0.11257192f, -0.03601262f, -0.011238519f, 0.040994775f, -0.10162282f, -0.08512377f, -0.03269827f, 0.07924202f, 0.0024967475f, 0.03605842f, -0.014996291f, 0.007802719f, -0.1157832f, 0.08059474f, 0.05741754f, -0.17340574f, 0.17910726f, -0.021425547f, -0.032907136f, -0.12506996f, 0.12061642f, -0.0016878322f, -0.1871461f, 0.0029128713f, -0.012999811f, 0.00808618f, 0.074498475f, -0.054447196f, -0.033595227f, 0.07466383f, 0.01507949f, 0.0309527f, -0.020618053f, -0.01079914f, 0.04137508f, -0.218215f, 0.007103635f, 0.16148832f, 0.06885385f, 0.024241306f, 0.048640005f, 0.04611632f, 0.044064824f, -0.012822341f, -0.03974728f, -0.031102201f, 0.117984384f, -0.027470494f, 0.13271357f, 0.040018026f, 0.03337095f, 0.026869498f, 0.19496214f, 0.008211367f, 0.10239701f, 0.07253066f, 0.16046412f, -0.14399268f, 0.022480235f, 0.20286082f, 0.079134494f, 0.07041231f, 0.03766456f};
    private float[] lwxFeature = new float[]{0.03583083f, 0.07690205f, 0.1329507f, 0.018072091f, 0.054815754f, -0.20668663f, 0.1252898f, -0.06090119f, 0.07204037f, -0.087241925f, 0.1640282f, -0.099476784f, -0.016362399f, -0.029536977f, 0.1537013f, 0.04517498f, 0.13228294f, 0.0723186f, -0.035422266f, -0.04156684f, -0.08766346f, 0.026997084f, 0.005762648f, 0.08820851f, -0.02937927f, 0.03426436f, 0.052179713f, -0.003562885f, 0.02116771f, 0.059405144f, -0.12828654f, -0.05359342f, -0.01882672f, -0.059600174f, 0.12045169f, 0.012934224f, 0.04470915f, 0.041303005f, -0.025705887f, -0.16045924f, -0.06060552f, 0.06460097f, -0.07279772f, 0.2125894f, 0.023260318f, 0.0063663763f, -0.030636523f, -0.2698398f, -0.0041989554f, 0.072114535f, 0.10904638f, -0.03489407f, -0.014457634f, -0.10707196f, 0.0046579456f, 0.04143595f, -0.085135676f, -0.04248417f, 0.086804405f, 0.055738926f, 0.020673756f, 0.023929017f, -0.03359669f, -0.14888372f, 0.010213742f, 0.13523556f, 0.0068953414f, 0.1633502f, -0.014467328f, 0.04889372f, -0.083336845f, 2.6942044E-4f, -0.016428f, 0.076757975f, -0.15568225f, 0.099361315f, -0.0814834f, -0.13310412f, 0.2032872f, 0.07400507f, 0.046281286f, -0.05591115f, -0.045955345f, 0.14707531f, 0.03231326f, -0.113599665f, 0.012298245f, 0.060331024f, -0.09695829f, -0.03067598f, 0.09534211f, -0.0337745f, -0.02550372f, -0.01776646f, -0.01866648f, 0.092251286f, -0.05803767f, 0.018343631f, 0.03846206f, -0.081054986f, -0.12381453f, -0.12998737f, 0.08835572f, -0.13927276f, -0.14938086f, 0.10870892f, -0.027882138f, 0.088126995f, -0.0065862443f, 0.07297076f, 0.042606477f, 0.034483958f, -0.052895896f, 0.10433018f, 0.024730654f, -0.0481632f, 0.015735196f, -0.050111935f, 0.011425007f, 0.14290316f, -0.016084542f, 0.23750357f, -0.07337758f, 0.06569163f, -0.036856603f, -0.040103015f, -0.17636979f, 0.017363591f};
    private float[] cgxFeature = new float[]{-0.076559946f, 6.7148905E-4f, -0.111485064f, -0.09346044f, -0.018380122f, -0.02724705f, 0.008616284f, -0.16656998f, 0.111131296f, 0.07401668f, -0.0441759f, 0.06394226f, 0.12776373f, -0.021518715f, -0.14547713f, -0.02256131f, 0.046619147f, -0.04492457f, -0.005321009f, -0.08481364f, 0.020196684f, 0.036529288f, 0.08580776f, -0.025896559f, -0.098402314f, -0.06904796f, -0.11215452f, -0.026327193f, -0.018463116f, 0.16366826f, 0.14768547f, -0.018923827f, 0.044984452f, -0.012872894f, -0.12146218f, -0.08228335f, -0.019814478f, 0.023257334f, 0.054002006f, -0.02109473f, 0.080047965f, -0.0040523526f, 0.02300355f, 0.031506073f, 0.037358418f, -0.15137118f, 0.09200838f, 0.19256224f, 0.022249155f, -0.13165031f, -0.11019112f, -0.0535264f, 0.03365853f, -0.069875285f, -0.09192192f, -0.08806229f, 0.12595524f, 0.03327776f, -0.005181481f, -0.050914586f, 0.035442304f, -0.03750162f, 0.110797435f, -0.05758486f, 0.0435137f, 0.06288246f, -0.09722555f, 0.043782104f, -0.0685802f, -0.11804007f, 0.011849842f, 0.16117573f, -0.014684636f, -0.064263284f, 0.1279025f, -8.1231206E-4f, 0.027093042f, 0.047399692f, 0.020837264f, -0.14506584f, 0.10929092f, 0.0404435f, 0.10085753f, -0.11651393f, -0.13618058f, 0.12190106f, -0.0071436637f, 0.15420485f, 0.14436235f, 0.12345447f, -0.17617196f, 0.08629374f, -0.128383f, 0.1550475f, 0.08641004f, -0.053706877f, 0.043412615f, -0.1521774f, 0.057349443f, -0.09393343f, -0.13969354f, 0.029236142f, -0.1166174f, -0.0761027f, -0.10050515f, -0.097735934f, -0.10198079f, 0.19978657f, 0.056568623f, -0.0810194f, 0.012343808f, -0.019763377f, 0.014757847f, -0.077013835f, -0.00780858f, -0.07165828f, 0.06829198f, 0.115218565f, -0.06573042f, 0.055454247f, -0.082608834f, -0.084712714f, 0.10388166f, -0.0055553117f, -0.14965975f, -0.07976972f, -0.052331366f, 0.08659784f};
    private float[] lhFeature = new float[]{-0.0763778f, 0.08399678f, 0.08825376f, 0.08195659f, -0.032671567f, -0.04247238f, 0.08671885f, -0.063615836f, -0.018159807f, -0.025843274f, 0.087482005f, -0.018795785f, 0.09881426f, 0.019607743f, -0.04191244f, -0.1403113f, 0.1004773f, -0.15992492f, 0.017212408f, -0.09950877f, -0.08036196f, 0.11098984f, -0.018937504f, -0.09579501f, -0.01279972f, -0.07619503f, 0.03536195f, 0.021939041f, 0.059534162f, 0.08087363f, 0.089684226f, 0.097410366f, 0.052883618f, 0.04619221f, -0.012280336f, -0.029450325f, 0.043763768f, 0.07883902f, 0.06352811f, 0.116683304f, 0.06598743f, -0.06148367f, -0.03229741f, 0.17697011f, -0.059752222f, 0.045637436f, -0.012109233f, 0.12042587f, 0.18377398f, -0.083601795f, -0.04141412f, -0.046178315f, -0.08875281f, -0.06781667f, -0.0103489505f, 0.027422043f, -0.06461115f, 0.19461925f, 0.007940547f, -0.1346538f, 0.17315792f, -0.04014374f, -0.019411337f, -0.116541095f, 0.1576f, 0.05647259f, 0.20112157f, 0.08430537f, 0.10417151f, -0.010022888f, -0.17680041f, 0.033659875f, 0.03432248f, -0.0035406419f, -0.07003502f, -0.0958734f, 0.076379485f, -0.044813797f, -0.12689382f, -0.019377593f, -0.10958498f, 0.09627468f, 0.052438144f, 0.07001976f, -0.17511985f, 0.045253437f, 0.029616565f, -0.1658451f, -0.06837577f, 0.08503924f, -0.025643377f, 0.10488804f, -0.09473734f, 0.123761326f, 0.0033670021f, 0.12889382f, -0.07896015f, -0.14802976f, 0.044083167f, 0.0099284025f, -0.020369332f, -0.07475792f, -0.15432043f, 0.090556815f, 0.031526677f, 0.07925897f, 0.18676272f, -0.10067335f, 0.13091029f, -0.07355525f, -0.008974816f, -0.088105f, -0.016003937f, 0.08302925f, -0.03757857f, -0.14583713f, -0.064394005f, -0.009481912f, -0.00836179f, -0.0020418328f, -0.01675014f, -0.02833305f, 0.11277966f, -0.088565335f, 0.008020937f, -0.060987122f, -0.105613984f, -0.1309053f};
    private float[] zyxFeature = new float[]{0.030386839f, 0.032270495f, 0.034705307f, 0.13974336f, 0.11458334f, 0.016831689f, 0.00797972f, -0.07363798f, -0.095874116f, -0.10763911f, 0.015441273f, 0.12755397f, 0.07435499f, -0.16063926f, -0.06876205f, -0.042675234f, -0.030139603f, 0.019014662f, -0.023270737f, -0.086517595f, -0.04030171f, -0.14808439f, -0.036341168f, -0.0968618f, -0.101032205f, -0.09755361f, 0.14855471f, 0.035212792f, 0.07078513f, 0.09662925f, 0.101733774f, -0.08909444f, -0.06442041f, 0.07501932f, -0.08455984f, -0.079022594f, 0.076729886f, 0.03866823f, -0.12578766f, -0.038148616f, 0.11561575f, 0.020702556f, 0.0036787624f, 0.09386538f, -0.010826112f, 0.0040944098f, -0.061387867f, 0.0015315488f, -0.057869744f, 0.064056166f, 0.0089154495f, -0.0055815545f, -0.17632052f, 0.14896739f, 0.03804997f, 0.09749597f, 0.115375705f, -0.14283922f, -0.07916961f, -0.059063982f, 6.713516E-4f, 0.005803574f, 0.18430136f, 0.24825482f, 0.0436315f, 0.13869444f, 0.060955446f, -0.13000508f, -0.08890492f, -0.22327064f, -0.05141787f, 0.046518724f, 0.08267957f, 0.052170396f, -0.010093417f, -0.024601355f, -0.03475528f, -0.003721637f, 0.19769451f, -0.14706405f, 0.06660177f, -0.026840916f, 0.058837425f, -0.029386438f, -0.071071595f, -0.08361668f, -0.14858164f, 0.041386355f, 0.07640666f, 0.03290316f, 0.019032175f, -0.02792297f, -0.13911647f, 0.017342543f, 6.974661E-5f, 0.05830741f, -0.10238601f, -0.03965799f, -0.030277189f, 0.03376742f, 0.031951487f, 0.12598509f, -0.12259549f, 0.016036514f, -0.06949823f, -0.04031459f, -0.06192312f, 0.05102692f, -0.013604771f, 0.037736047f, 0.08433059f, -0.088220686f, -0.10586555f, 0.08642963f, 0.067550644f, -0.008634864f, 0.04361098f, 0.11493418f, -0.10920048f, -0.062446732f, -0.09472297f, -0.110241964f, 0.15038458f, -0.006571094f, -0.09704737f, 0.089564316f, -0.18345241f, 0.031468492f};
    private float[] wyfFeature = new float[]{-0.12740177f, -0.036678113f, -0.09756238f, -0.0015223026f, 0.1401979f, -0.16794689f, 0.043026645f, -0.023795124f, -0.008499103f, -0.090849794f, 0.01148872f, -0.08167699f, 0.030433547f, 0.012177954f, 0.08568856f, 0.08019901f, 0.040322084f, -0.114618346f, 0.01480283f, 0.06786888f, -0.09073869f, -0.1375541f, 0.08780506f, -0.09772659f, -0.07450093f, 0.0038166172f, 0.09479263f, -0.07057609f, -0.06724668f, 0.15297581f, -0.045835014f, 0.11343513f, 0.11628367f, 0.21403521f, 0.15615313f, 0.03794611f, 0.076637186f, 0.058712013f, -0.020715667f, -0.025627067f, 0.04088188f, -0.12755063f, -0.04230599f, 0.045431796f, -0.0018870448f, 0.04336817f, -0.0028479365f, 0.084693536f, 0.018726215f, -0.095578685f, 0.05966536f, -0.053653136f, -0.122350134f, -0.0798345f, -0.02807145f, -0.019763753f, 0.0071240524f, 0.09068783f, -0.102708966f, 0.0970419f, 0.007932037f, 0.06796754f, 0.0020810226f, -0.06167971f, -0.102257244f, -0.044441182f, 0.17901799f, -0.09387332f, -0.0023413142f, -0.06648162f, 0.10210609f, 0.019437555f, -0.0808522f, -0.055696093f, -0.05844578f, 0.03197245f, 0.031526186f, -0.009079091f, -0.031844873f, -0.008383402f, -0.03202626f, 0.005751586f, 0.17433341f, 0.120556265f, -0.06364572f, -0.0791644f, -0.0014456515f, 0.027264087f, 0.014996059f, 0.15680435f, 0.07485395f, 0.1431574f, -0.06476475f, 0.08644089f, 0.06327786f, 0.12259981f, 0.018339023f, -0.090327285f, 0.052675188f, -0.12422191f, -0.031359423f, -0.009288368f, -0.122757375f, -0.066229485f, -0.15610723f, 0.047719862f, 0.12492867f, 0.15785252f, 0.11803818f, -0.008692819f, 0.0824683f, -0.023834588f, -0.06691436f, 0.066392645f, 0.104758784f, -0.002628272f, 0.15413973f, 0.088211946f, -0.037110046f, 0.12433049f, 0.06682147f, -0.27508587f, 0.021311972f, -0.10979017f, -0.10111664f, -0.14833729f, -0.108389154f, 0.023674693f};

    private float[] preFeature = null;
    private String preFaceName = "";

    private double threshold = 0.5;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!hasPermission()) {
            requestPermission();
        }
        setContentView(R.layout.activity_face);
        initAllView();
        initModel();
    }

    private void initAllView() {
        tv_res = findViewById(R.id.tv_res);
        bt_switch_camera = findViewById(R.id.bt_switch_camera);
        bt_switch_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFont = !isFont;
                stopInfer();
                initStatus();
            }
        });
        mTextureView = findViewById(R.id.texture_view);
        mTextureView.setAspectRatio(4, 3);
//        mDrawView = findViewById(R.id.drawView);
        mSurfaceView = findViewById(R.id.surfaceview);
        mSurfaceView.setZOrderOnTop(true);//处于顶层
        mSurfaceView.getHolder().setFormat(PixelFormat.TRANSPARENT);//设置surface为透明
        mSurfaceHolder = mSurfaceView.getHolder();
    }

    private void initModel() {
        mFaceRecognize = new FaceRecognize();
        mFaceRecognize.initRetainFace(getAssets());

        //拷贝模型到sd卡
        String sdPath = getCacheDir().getAbsolutePath() + "/facem/";
        Utils.copyFileFromAsset(this, "mobilefacenet.bin", sdPath + File.separator + "mobilefacenet.bin");
        Utils.copyFileFromAsset(this, "mobilefacenet.param", sdPath + File.separator + "mobilefacenet.param");
        //模型初始化
        mFaceRecognize.initMobileFacenet(sdPath);
    }

    // 预测图片线程
    private Runnable periodicClassify =
            new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        if (runClassifier) {
                            // 开始预测前要判断相机是否已经准备好
                            if (getApplicationContext() != null && mCameraDevice != null) {
                                predict();
                            }
                        }
                    }
                    if (mInferThread != null && mInferHandler != null && mCaptureHandler != null && mCaptureThread != null) {
                        mInferHandler.post(periodicClassify);  // 相当于回调自身，递归调用自身
                    }
                }
            };


    // 预测相机捕获的图像
    private void predict() {
        // 获取相机捕获的图像
        Bitmap bitmap = mTextureView.getBitmap();
        try {
            // 预测图像 c h w : 3 1445 1080
//            float[] result = mRetinaFace.detectFromBitmap(bitmap);
            Log.d(TAG, "predict: Bitmap 原尺寸 w:" + bitmap.getWidth() + ", h:" + bitmap.getHeight());
            float[][] result = mFaceRecognize.detectTest(bitmap, bitmap.getWidth(), bitmap.getHeight(), 3);
            if (result == null) {
                drawRectBySurface(null);
                preFeature = null;
                preFaceName = "没有人噢";
                tv_res.setText(preFaceName);
                return;
            }
            Log.d(TAG, "predict: result.length => " + result.length);
            if (result.length != 0) {
                Log.d(TAG, "predict: 检测到几张人脸？ => " + result.length);
                FaceInfo faceInfos[] = new FaceInfo[result.length];
                for (int i = 0; i < faceInfos.length; i++) {
                    FaceInfo faceInfo = CommonUtils.floatArr2FaceInfo(result[i]);
                    faceInfos[i] = faceInfo;
                }
                drawRectBySurface(faceInfos);
                // 测试提取人脸特征
                test(bitmap, mTextureView.getWidth(), mTextureView.getHeight(), CommonUtils.getUsefulLandmarksFromFaceInfo(faceInfos[0]));
            } else {
                drawRectBySurface(null);
                preFeature = null;
                preFaceName = "没有人噢";
                tv_res.setText(preFaceName);
            }
            // --------------------- 下面是已弃用的方案 --------------------
            // mDrawView.addFaceRect(faceInfos);
            // -----------------------------------------------------------
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void test(Bitmap bitmap, int w, int h, int[] landmarks) {
//        Log.d(TAG, "byteArr2BitmapAndLogRecognizeResult: 人脸特征提取开始");
        float features[] = mFaceRecognize.recognize(CommonUtils.getPixelsRGBA(bitmap), w, h, landmarks);
//        Log.d(TAG, "featuresArr: " + CommonUtils.showArr(features));
//        Log.d(TAG, "byteArr2BitmapAndLogRecognizeResult: 人脸特征提取完成");

        if (preFeature != null && mFaceRecognize.compare(features, preFeature) >= threshold) {
            tv_res.setText(preFaceName);
            return;
        }

        boolean zzfFlag = mFaceRecognize.compare(features, zzfFeature) >= threshold;
        if (zzfFlag) {
            preFeature = zzfFeature;
            preFaceName = "钟*锋";
            tv_res.setText(preFaceName);
            return;
        }

        boolean zljFlag = mFaceRecognize.compare(features, zljFeature) >= threshold;
        if (zljFlag) {
            preFeature = zljFeature;
            preFaceName = "钟*基";
            tv_res.setText(preFaceName);
            return;
        }

        boolean zstFlag = mFaceRecognize.compare(features, zstFeature) >= threshold;
        if (zstFlag) {
            preFeature = zstFeature;
            preFaceName = "钟*亭";
            tv_res.setText(preFaceName);
            return;
        }

        boolean lwxFlag = mFaceRecognize.compare(features, lwxFeature) >= threshold;
        if (lwxFlag) {
            preFeature = lwxFeature;
            preFaceName = "梁**";
            tv_res.setText(preFaceName);
            return;
        }

        boolean cgxFlag = mFaceRecognize.compare(features, cgxFeature) >= threshold;
        if (cgxFlag) {
            preFeature = lwxFeature;
            preFaceName = "陈冠希";
            tv_res.setText(preFaceName);
            return;
        }

        boolean lhFlag = mFaceRecognize.compare(features, lhFeature) >= threshold;
        if (lhFlag) {
            preFeature = lwxFeature;
            preFaceName = "鹿晗";
            tv_res.setText(preFaceName);
            return;
        }

        boolean zyxFlag = mFaceRecognize.compare(features, zyxFeature) >= threshold;
        if (zyxFlag) {
            preFeature = lwxFeature;
            preFaceName = "张艺兴";
            tv_res.setText(preFaceName);
            return;
        }

        boolean wyfFlag = mFaceRecognize.compare(features, wyfFeature) >= threshold;
        if (wyfFlag) {
            preFeature = lwxFeature;
            preFaceName = "吴亦凡";
            tv_res.setText(preFaceName);
            return;
        }

        preFeature = null;
        preFaceName = "这谁啊？？？";
        tv_res.setText(preFaceName);

        //        Log.d(TAG, "byteArr2BitmapAndLogRecognizeResult: 是同一个人吗？: " + (flag ? "是" : "不是"));
    }

    private static Paint rectPaint = new Paint();
    private static Paint pointPaint = new Paint();

    static {
        rectPaint.setColor(Color.GREEN);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(4);
        pointPaint.setColor(Color.GREEN);
        pointPaint.setStyle(Paint.Style.FILL);
    }

    private void drawRectBySurface(FaceInfo[] faceInfos) {
        if (faceInfos == null) {
            Canvas canvas = new Canvas();
            canvas = mSurfaceHolder.lockCanvas();
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR); //清楚掉上一次的画框。
            mSurfaceHolder.unlockCanvasAndPost(canvas);
            return;
        }
        Canvas canvas = new Canvas();
        canvas = mSurfaceHolder.lockCanvas();
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR); //清楚掉上一次的画框。
        for (int i = 0; i < faceInfos.length; i++) {
            canvas.drawRect(faceInfos[i].x1, faceInfos[i].y1, faceInfos[i].x2, faceInfos[i].y2, rectPaint);
            for (int j = 0; j < 5; j++) {
                canvas.drawCircle(faceInfos[i].keypoints[j][0], faceInfos[i].keypoints[j][1], 6f, pointPaint);
            }
        }
        mSurfaceHolder.unlockCanvasAndPost(canvas);
        return;
    }


    // 初始化以下变量和状态
    private void initStatus() {
        // 启动线程
        startCaptureThread();
        startInferThread();

        // 判断SurfaceTexture是否可用，可用就直接启动捕获图片
        if (mTextureView.isAvailable()) {
            startCapture();
        } else {
            mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    startCapture();
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    return true;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                }
            });
        }
    }

    // 启动捕获图片
    private void startCapture() {
        // 判断是否正处于捕获图片的状态
        if (mCapturing) return;
        mCapturing = true;

        final CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        // 查看可用的摄像头
        String cameraIdAvailable = null;
        try {
            assert manager != null;
            for (final String cameraId : manager.getCameraIdList()) {
                final CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

                final Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                // 设置相机前摄像头或者后摄像头
                if (isFont) {
                    if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                        cameraIdAvailable = cameraId;
                        break;
                    }
                } else {
                    if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                        cameraIdAvailable = cameraId;
                        break;
                    }
                }
            }
        } catch (CameraAccessException e) {
            Log.e(TAG, "启动图片捕获异常 ", e);
        }

        // 开启摄像头
        try {
            assert cameraIdAvailable != null;
            final CameraCharacteristics characteristics =
                    manager.getCameraCharacteristics(cameraIdAvailable);

            final StreamConfigurationMap map =
                    characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            mPreviewSize = CameraUtils.chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                    mTextureView.getWidth(),
                    mTextureView.getHeight());
            Log.d("mPreviewSize", String.valueOf(mPreviewSize));
            mTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
            manager.openCamera(cameraIdAvailable, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    mCameraDevice = camera;
                    createCaptureSession();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    camera.close();
                    mCameraDevice = null;
                    mCapturing = false;
                }

                @Override
                public void onError(@NonNull CameraDevice camera, final int error) {
                    Log.e(TAG, "打开相机错误 =  " + error);
                    camera.close();
                    mCameraDevice = null;
                    mCapturing = false;
                }
            }, mCaptureHandler);
        } catch (CameraAccessException | SecurityException e) {
            mCapturing = false;
            Log.e(TAG, "启动图片捕获异常 ", e);
        }
    }

    // 创建捕获图片session
    // 设置为“预览模式”，设置一些相机参数，设置消息处理者 mCaptureHandler
    private void createCaptureSession() {
        try {
            final SurfaceTexture texture = mTextureView.getSurfaceTexture();
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

            final Surface surface = new Surface(texture);
            final CaptureRequest.Builder captureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);

            mImageReader = ImageReader.newInstance(
                    mPreviewSize.getWidth(), mPreviewSize.getHeight(), ImageFormat.JPEG, 10);

            mCameraDevice.createCaptureSession(
                    Arrays.asList(surface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            if (null == mCameraDevice) {
                                return;
                            }

                            mCaptureSession = cameraCaptureSession;
                            try {
                                captureRequestBuilder.set(
                                        CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                captureRequestBuilder.set(
                                        CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

                                CaptureRequest previewRequest = captureRequestBuilder.build();
                                mCaptureSession.setRepeatingRequest(
                                        previewRequest, new CameraCaptureSession.CaptureCallback() {
                                            @Override
                                            public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
                                                super.onCaptureProgressed(session, request, partialResult);
                                            }

                                            @Override
                                            public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
                                                super.onCaptureFailed(session, request, failure);
                                                Log.d(TAG, "onCaptureFailed = " + failure.getReason());
                                            }

                                            @Override
                                            public void onCaptureSequenceCompleted(@NonNull CameraCaptureSession session, int sequenceId, long frameNumber) {
                                                super.onCaptureSequenceCompleted(session, sequenceId, frameNumber);
                                                Log.d(TAG, "onCaptureSequenceCompleted");
                                            }
                                        }, mCaptureHandler);
                            } catch (final CameraAccessException e) {
                                Log.e(TAG, "onConfigured exception ", e);
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull final CameraCaptureSession cameraCaptureSession) {
                            Log.e(TAG, "onConfigureFailed ");
                        }
                    },
                    null);
        } catch (final CameraAccessException e) {
            Log.e(TAG, "创建捕获图片session异常 ", e);
        }
    }

    // 关闭相机
    private void closeCamera() {
        if (mCaptureSession != null) {
            mCaptureSession.close();
            mCaptureSession = null;
        }
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }
        mCapturing = false;
    }

    // 关闭捕获图片线程
    private void stopCaptureThread() {
        try {
            if (mCaptureThread != null) {
                mCaptureThread.quitSafely();
                mCaptureThread.join();
            }
            mCaptureThread = null;
            mCaptureHandler = null;
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    // 关闭预测线程
    private void stopInferThread() {
        try {
            if (mInferThread != null) {
                mInferThread.quitSafely();
                mInferThread.join();
            }
            mInferThread = null;
            mInferHandler = null;
            synchronized (lock) {
                runClassifier = false;
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        stopInfer();
        super.onPause();
    }

    @Override
    protected void onStop() {
        stopInfer();
        super.onStop();
    }

    // 停止预测操作
    private void stopInfer() {
        // 关闭相机和线程
        closeCamera();
        stopCaptureThread();
        stopInferThread();
    }

    // 启动捕获图片线程
    private void startCaptureThread() {
        mCaptureThread = new HandlerThread("capture");
        mCaptureThread.start();
        mCaptureHandler = new Handler(mCaptureThread.getLooper());
    }

    // 启动预测线程
    private void startInferThread() {
        mInferThread = new HandlerThread("inference");
        mInferThread.start();
        mInferHandler = new Handler(mInferThread.getLooper());
        synchronized (lock) {
            runClassifier = true;
        }
        mInferHandler.post(periodicClassify);
    }

    @Override
    protected void onResume() {
        initStatus();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // check had permission
    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    // request permission
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

}
