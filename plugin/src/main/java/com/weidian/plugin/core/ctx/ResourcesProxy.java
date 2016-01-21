package com.weidian.plugin.core.ctx;

import android.annotation.TargetApi;
import android.content.res.*;
import android.graphics.Movie;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import com.weidian.plugin.PluginManager;

import java.io.InputStream;

/*package*/ final class ResourcesProxy extends Resources {

	private final Resources appRes;
	private static final int MODULE_RES_ID_BEGIN = 0x7f000000;

	/**
	 * Create a new Resources object on top of an existing set of assets in an
	 * AssetManager.
	 *
	 * @param assets  Previously created AssetManager.
	 * @param metrics Current display metrics to consider when
	 *                selecting/computing resource values.
	 * @param config  Desired device configuration to consider when
	 */
	public ResourcesProxy(AssetManager assets, DisplayMetrics metrics, Configuration config) {
		super(assets, metrics, config);
		this.appRes = PluginManager.getApplication().getResources();
	}

	@Override
	public CharSequence getText(int id) throws NotFoundException {
		if (id < MODULE_RES_ID_BEGIN) {
			return appRes.getText(id);
		} else {
			return super.getText(id);
		}
	}

	@Override
	public CharSequence getQuantityText(int id, int quantity) throws NotFoundException {
		if (id < MODULE_RES_ID_BEGIN) {
			return appRes.getQuantityText(id, quantity);
		} else {
			return super.getQuantityText(id, quantity);
		}
	}

	@Override
	public String getString(int id) throws NotFoundException {
		if (id < MODULE_RES_ID_BEGIN) {
			return appRes.getString(id);
		} else {
			return super.getString(id);
		}
	}

	@Override
	public String getString(int id, Object... formatArgs) throws NotFoundException {
		if (id < MODULE_RES_ID_BEGIN) {
			return appRes.getString(id, formatArgs);
		} else {
			return super.getString(id, formatArgs);
		}
	}

	@Override
	public String getQuantityString(int id, int quantity, Object... formatArgs) throws NotFoundException {
		if (id < MODULE_RES_ID_BEGIN) {
			return appRes.getQuantityString(id, quantity, formatArgs);
		} else {
			return super.getQuantityString(id, quantity, formatArgs);
		}
	}

	@Override
	public String getQuantityString(int id, int quantity) throws NotFoundException {
		if (id < MODULE_RES_ID_BEGIN) {
			return appRes.getQuantityString(id, quantity);
		} else {
			return super.getQuantityString(id, quantity);
		}
	}

	@Override
	public CharSequence getText(int id, CharSequence def) {
		if (id < MODULE_RES_ID_BEGIN) {
			return appRes.getText(id, def);
		} else {
			return super.getText(id, def);
		}
	}

	@Override
	public CharSequence[] getTextArray(int id) throws NotFoundException {
		if (id < MODULE_RES_ID_BEGIN) {
			return appRes.getTextArray(id);
		} else {
			return super.getTextArray(id);
		}
	}

	@Override
	public String[] getStringArray(int id) throws NotFoundException {
		if (id < MODULE_RES_ID_BEGIN) {
			return appRes.getStringArray(id);
		} else {
			return super.getStringArray(id);
		}
	}

	@Override
	public int[] getIntArray(int id) throws NotFoundException {
		if (id < MODULE_RES_ID_BEGIN) {
			return appRes.getIntArray(id);
		} else {
			return super.getIntArray(id);
		}
	}

	@Override
	public TypedArray obtainTypedArray(int id) throws NotFoundException {
		if (id < MODULE_RES_ID_BEGIN) {
			return appRes.obtainTypedArray(id);
		} else {
			return super.obtainTypedArray(id);
		}
	}

	@Override
	public float getDimension(int id) throws NotFoundException {
		if (id < MODULE_RES_ID_BEGIN) {
			return appRes.getDimension(id);
		} else {
			return super.getDimension(id);
		}
	}

	@Override
	public int getDimensionPixelOffset(int id) throws NotFoundException {
		if (id < MODULE_RES_ID_BEGIN) {
			return appRes.getDimensionPixelOffset(id);
		} else {
			return super.getDimensionPixelOffset(id);
		}
	}

	@Override
	public int getDimensionPixelSize(int id) throws NotFoundException {
		if (id < MODULE_RES_ID_BEGIN) {
			return appRes.getDimensionPixelSize(id);
		} else {
			return super.getDimensionPixelSize(id);
		}
	}

	@Override
	public float getFraction(int id, int base, int pbase) {
		if (id < MODULE_RES_ID_BEGIN) {
			return appRes.getFraction(id, base, pbase);
		} else {
			return super.getFraction(id, base, pbase);
		}
	}

	@Override
	public Drawable getDrawable(int id) throws NotFoundException {
		if (id < MODULE_RES_ID_BEGIN) {
			return appRes.getDrawable(id);
		} else {
			return super.getDrawable(id);
		}
	}

	@Override
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
	public Drawable getDrawableForDensity(int id, int density) throws NotFoundException {
		if (id < MODULE_RES_ID_BEGIN) {
			return appRes.getDrawableForDensity(id, density);
		} else {
			return super.getDrawableForDensity(id, density);
		}
	}

	@Override
	public Movie getMovie(int id) throws NotFoundException {
		if (id < MODULE_RES_ID_BEGIN) {
			return appRes.getMovie(id);
		} else {
			return super.getMovie(id);
		}
	}

	@Override
	public int getColor(int id) throws NotFoundException {
		if (id < MODULE_RES_ID_BEGIN) {
			return appRes.getColor(id);
		} else {
			return super.getColor(id);
		}
	}

	@Override
	public ColorStateList getColorStateList(int id) throws NotFoundException {
		if (id < MODULE_RES_ID_BEGIN) {
			return appRes.getColorStateList(id);
		} else {
			return super.getColorStateList(id);
		}
	}

	@Override
	public boolean getBoolean(int id) throws NotFoundException {
		if (id < MODULE_RES_ID_BEGIN) {
			return appRes.getBoolean(id);
		} else {
			return super.getBoolean(id);
		}
	}

	@Override
	public int getInteger(int id) throws NotFoundException {
		if (id < MODULE_RES_ID_BEGIN) {
			return appRes.getInteger(id);
		} else {
			return super.getInteger(id);
		}
	}

	@Override
	public XmlResourceParser getLayout(int id) throws NotFoundException {
		if (id < MODULE_RES_ID_BEGIN) {
			return appRes.getLayout(id);
		} else {
			return super.getLayout(id);
		}
	}

	@Override
	public XmlResourceParser getAnimation(int id) throws NotFoundException {
		if (id < MODULE_RES_ID_BEGIN) {
			return appRes.getAnimation(id);
		} else {
			return super.getAnimation(id);
		}
	}

	@Override
	public XmlResourceParser getXml(int id) throws NotFoundException {
		if (id < MODULE_RES_ID_BEGIN) {
			return appRes.getXml(id);
		} else {
			return super.getXml(id);
		}
	}

	@Override
	public InputStream openRawResource(int id) throws NotFoundException {
		if (id < MODULE_RES_ID_BEGIN) {
			return appRes.openRawResource(id);
		} else {
			return super.openRawResource(id);
		}
	}

	@Override
	public InputStream openRawResource(int id, TypedValue value) throws NotFoundException {
		if (id < MODULE_RES_ID_BEGIN) {
			return appRes.openRawResource(id, value);
		} else {
			return super.openRawResource(id, value);
		}
	}

	@Override
	public AssetFileDescriptor openRawResourceFd(int id) throws NotFoundException {
		if (id < MODULE_RES_ID_BEGIN) {
			return appRes.openRawResourceFd(id);
		} else {
			return super.openRawResourceFd(id);
		}
	}

	@Override
	public void getValue(int id, TypedValue outValue, boolean resolveRefs) throws NotFoundException {
		if (id < MODULE_RES_ID_BEGIN) {
			appRes.getValue(id, outValue, resolveRefs);
		} else {
			super.getValue(id, outValue, resolveRefs);
		}
	}

	@Override
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
	public void getValueForDensity(int id, int density, TypedValue outValue, boolean resolveRefs) throws NotFoundException {
		if (id < MODULE_RES_ID_BEGIN) {
			appRes.getValueForDensity(id, density, outValue, resolveRefs);
		} else {
			super.getValueForDensity(id, density, outValue, resolveRefs);
		}
	}

	@Override
	public String getResourceName(int resid) throws NotFoundException {
		if (resid < MODULE_RES_ID_BEGIN) {
			return appRes.getResourceName(resid);
		} else {
			return super.getResourceName(resid);
		}
	}

	@Override
	public String getResourcePackageName(int resid) throws NotFoundException {
		if (resid < MODULE_RES_ID_BEGIN) {
			return appRes.getResourcePackageName(resid);
		} else {
			return super.getResourcePackageName(resid);
		}
	}

	@Override
	public String getResourceTypeName(int resid) throws NotFoundException {
		if (resid < MODULE_RES_ID_BEGIN) {
			return appRes.getResourceTypeName(resid);
		} else {
			return super.getResourceTypeName(resid);
		}
	}

	@Override
	public String getResourceEntryName(int resid) throws NotFoundException {
		if (resid < MODULE_RES_ID_BEGIN) {
			return appRes.getResourceEntryName(resid);
		} else {
			return super.getResourceEntryName(resid);
		}
	}

	@Override
	public int getIdentifier(String name, String defType, String defPackage) {
		int result = super.getIdentifier(name, defType, defPackage);
		if (result < 1) {
			result = appRes.getIdentifier(name, defType, PluginManager.getApplication().getPackageName());
		}
		return result;
	}
}
