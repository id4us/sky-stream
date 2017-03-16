package com.sky.mobile.skytvstream.service.secure;


import com.google.common.base.Optional;
import com.sky.mobile.skytvstream.domain.StreamKeyVo;

public interface StreamKeyService {

     Optional<String> generateKey(final StreamKeyVo streamKeyVo);

     Optional<StreamKeyVo> getStreamKeyVo(final String streamKey);
}
