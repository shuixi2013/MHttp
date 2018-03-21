package im.wangchao.mhttp.adapter.rxjava2;

import im.wangchao.mhttp.Request;
import im.wangchao.mhttp.Response;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.CompositeException;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.plugins.RxJavaPlugins;

/**
 * <p>Description  : ResponseExecuteObservable.</p>
 * <p>Author       : wangchao.</p>
 * <p>Date         : 2018/3/19.</p>
 * <p>Time         : 下午4:46.</p>
 */
public class ResponseExecuteObservable extends Observable<Response> {
    private final Request request;

    ResponseExecuteObservable(Request request){
        this.request = request;
    }

    @Override protected void subscribeActual(Observer<? super Response> observer) {

        ExecuteDisposable disposable = new ExecuteDisposable(request);
        observer.onSubscribe(disposable);

        boolean terminated = false;
        try {
            Response response = request.execute();
            if (!disposable.isDisposed()) {
                observer.onNext(response);
            }
            if (!disposable.isDisposed()) {
                terminated = true;
                observer.onComplete();
            }
        } catch (Throwable t) {
            Exceptions.throwIfFatal(t);
            if (terminated) {
                RxJavaPlugins.onError(t);
            } else if (!disposable.isDisposed()) {
                try {
                    observer.onError(t);
                } catch (Throwable inner) {
                    Exceptions.throwIfFatal(inner);
                    RxJavaPlugins.onError(new CompositeException(t, inner));
                }
            }
        }

    }

    private static final class ExecuteDisposable implements Disposable {
        private final Request request;
        private volatile boolean disposed;

        ExecuteDisposable(Request request) {
            this.request = request;
        }

        @Override public void dispose() {
            disposed = true;
            request.cancel();
        }

        @Override public boolean isDisposed() {
            return disposed;
        }
    }
}
