package com.tillzo.pos;

import androidx.hilt.work.HiltWrapper_WorkerFactoryModule;
import com.tillzo.pos.data.sync.options.worker.DisasterWorker_HiltModule;
import com.tillzo.pos.data.sync.options.worker.ExpiryCheckWorker_HiltModule;
import com.tillzo.pos.data.sync.options.worker.ShardingWorker_HiltModule;
import com.tillzo.pos.data.sync.options.worker.SyncWorker_HiltModule;
import com.tillzo.pos.di.DatabaseModule;
import com.tillzo.pos.di.NetworkModule;
import com.tillzo.pos.di.RepositoryModule;
import com.tillzo.pos.di.SyncModule;
import com.tillzo.pos.di.WorkerModule;
import com.tillzo.pos.ui.MainActivity_GeneratedInjector;
import com.tillzo.pos.ui.auth.options.login.LoginViewModel_HiltModules;
import com.tillzo.pos.ui.auth.options.permissions.PermissionManagerViewModel_HiltModules;
import com.tillzo.pos.ui.auth.options.session.PINUnlockViewModel_HiltModules;
import com.tillzo.pos.ui.auth.options.usermanagement.UserManagementViewModel_HiltModules;
import com.tillzo.pos.ui.hardware.printer.PrinterSettingsViewModel_HiltModules;
import com.tillzo.pos.ui.hardware.scanner.InlineScannerViewModel_HiltModules;
import com.tillzo.pos.ui.hardware.scanner.ScannerViewModel_HiltModules;
import com.tillzo.pos.ui.home.HomeViewModel_HiltModules;
import com.tillzo.pos.ui.home.PosViewModel_HiltModules;
import com.tillzo.pos.ui.inventory.CategoryManagementViewModel_HiltModules;
import com.tillzo.pos.ui.inventory.ProductUnitsViewModel_HiltModules;
import com.tillzo.pos.ui.inventory.StockAdjustmentViewModel_HiltModules;
import com.tillzo.pos.ui.inventory.module_b.VendorManagementViewModel_HiltModules;
import com.tillzo.pos.ui.inventory.module_b.viewmodel.CreatePurchaseOrderViewModel_HiltModules;
import com.tillzo.pos.ui.inventory.module_b.viewmodel.PODetailViewModel_HiltModules;
import com.tillzo.pos.ui.inventory.module_b.viewmodel.PurchaseOrderListViewModel_HiltModules;
import com.tillzo.pos.ui.inventory.module_c.GrnDetailViewModel_HiltModules;
import com.tillzo.pos.ui.inventory.module_c.viewmodel.CreateGrnViewModel_HiltModules;
import com.tillzo.pos.ui.inventory.module_c.viewmodel.GrnListViewModel_HiltModules;
import com.tillzo.pos.ui.inventory.options.alerts.LowStockViewModel_HiltModules;
import com.tillzo.pos.ui.inventory.options.crud.InventoryCrudViewModel_HiltModules;
import com.tillzo.pos.ui.inventory.options.ocr.OcrEntryViewModel_HiltModules;
import com.tillzo.pos.ui.inventory.options.qr.QrGeneratorViewModel_HiltModules;
import com.tillzo.pos.ui.inventory.options.wastage.WastageViewModel_HiltModules;
import com.tillzo.pos.ui.pos.options.casio.CasioViewModel_HiltModules;
import com.tillzo.pos.ui.pos.options.checkout.CheckoutViewModel_HiltModules;
import com.tillzo.pos.ui.settings.options.billing.BillingViewModel_HiltModules;
import com.tillzo.pos.ui.settings.options.privacy.SettingsViewModel_HiltModules;
import com.tillzo.pos.ui.setup.SheetPickerViewModel_HiltModules;
import com.tillzo.pos.ui.signin.SignInViewModel_HiltModules;
import com.tillzo.pos.ui.store.options.crm.CrmViewModel_HiltModules;
import com.tillzo.pos.ui.store.options.expense.ExpenseViewModel_HiltModules;
import com.tillzo.pos.ui.store.options.history.HistoryViewModel_HiltModules;
import com.tillzo.pos.ui.store.options.returns.ReturnsViewModel_HiltModules;
import com.tillzo.pos.ui.store.options.statement.StatementViewModel_HiltModules;
import com.tillzo.pos.ui.store.options.zreport.ZReportViewModel_HiltModules;
import com.tillzo.pos.ui.till.TillViewModel_HiltModules;
import com.tillzo.pos.ui.update.ForceUpdateViewModel_HiltModules;
import dagger.Binds;
import dagger.Component;
import dagger.Module;
import dagger.Subcomponent;
import dagger.hilt.android.components.ActivityComponent;
import dagger.hilt.android.components.ActivityRetainedComponent;
import dagger.hilt.android.components.FragmentComponent;
import dagger.hilt.android.components.ServiceComponent;
import dagger.hilt.android.components.ViewComponent;
import dagger.hilt.android.components.ViewModelComponent;
import dagger.hilt.android.components.ViewWithFragmentComponent;
import dagger.hilt.android.flags.FragmentGetContextFix;
import dagger.hilt.android.flags.HiltWrapper_FragmentGetContextFix_FragmentGetContextFixModule;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.HiltViewModelFactory;
import dagger.hilt.android.internal.lifecycle.HiltWrapper_DefaultViewModelFactories_ActivityModule;
import dagger.hilt.android.internal.lifecycle.HiltWrapper_HiltViewModelFactory_ActivityCreatorEntryPoint;
import dagger.hilt.android.internal.lifecycle.HiltWrapper_HiltViewModelFactory_ViewModelModule;
import dagger.hilt.android.internal.managers.ActivityComponentManager;
import dagger.hilt.android.internal.managers.FragmentComponentManager;
import dagger.hilt.android.internal.managers.HiltWrapper_ActivityRetainedComponentManager_ActivityRetainedComponentBuilderEntryPoint;
import dagger.hilt.android.internal.managers.HiltWrapper_ActivityRetainedComponentManager_ActivityRetainedLifecycleEntryPoint;
import dagger.hilt.android.internal.managers.HiltWrapper_ActivityRetainedComponentManager_LifecycleModule;
import dagger.hilt.android.internal.managers.HiltWrapper_SavedStateHandleModule;
import dagger.hilt.android.internal.managers.ServiceComponentManager;
import dagger.hilt.android.internal.managers.ViewComponentManager;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.HiltWrapper_ActivityModule;
import dagger.hilt.android.scopes.ActivityRetainedScoped;
import dagger.hilt.android.scopes.ActivityScoped;
import dagger.hilt.android.scopes.FragmentScoped;
import dagger.hilt.android.scopes.ServiceScoped;
import dagger.hilt.android.scopes.ViewModelScoped;
import dagger.hilt.android.scopes.ViewScoped;
import dagger.hilt.components.SingletonComponent;
import dagger.hilt.internal.GeneratedComponent;
import dagger.hilt.migration.DisableInstallInCheck;
import javax.annotation.processing.Generated;
import javax.inject.Singleton;

@Generated("dagger.hilt.processor.internal.root.RootProcessor")
public final class TillzoPOSApp_HiltComponents {
  private TillzoPOSApp_HiltComponents() {
  }

  @Module(
      subcomponents = ServiceC.class
  )
  @DisableInstallInCheck
  @Generated("dagger.hilt.processor.internal.root.RootProcessor")
  abstract interface ServiceCBuilderModule {
    @Binds
    ServiceComponentBuilder bind(ServiceC.Builder builder);
  }

  @Module(
      subcomponents = ActivityRetainedC.class
  )
  @DisableInstallInCheck
  @Generated("dagger.hilt.processor.internal.root.RootProcessor")
  abstract interface ActivityRetainedCBuilderModule {
    @Binds
    ActivityRetainedComponentBuilder bind(ActivityRetainedC.Builder builder);
  }

  @Module(
      subcomponents = ActivityC.class
  )
  @DisableInstallInCheck
  @Generated("dagger.hilt.processor.internal.root.RootProcessor")
  abstract interface ActivityCBuilderModule {
    @Binds
    ActivityComponentBuilder bind(ActivityC.Builder builder);
  }

  @Module(
      subcomponents = ViewModelC.class
  )
  @DisableInstallInCheck
  @Generated("dagger.hilt.processor.internal.root.RootProcessor")
  abstract interface ViewModelCBuilderModule {
    @Binds
    ViewModelComponentBuilder bind(ViewModelC.Builder builder);
  }

  @Module(
      subcomponents = ViewC.class
  )
  @DisableInstallInCheck
  @Generated("dagger.hilt.processor.internal.root.RootProcessor")
  abstract interface ViewCBuilderModule {
    @Binds
    ViewComponentBuilder bind(ViewC.Builder builder);
  }

  @Module(
      subcomponents = FragmentC.class
  )
  @DisableInstallInCheck
  @Generated("dagger.hilt.processor.internal.root.RootProcessor")
  abstract interface FragmentCBuilderModule {
    @Binds
    FragmentComponentBuilder bind(FragmentC.Builder builder);
  }

  @Module(
      subcomponents = ViewWithFragmentC.class
  )
  @DisableInstallInCheck
  @Generated("dagger.hilt.processor.internal.root.RootProcessor")
  abstract interface ViewWithFragmentCBuilderModule {
    @Binds
    ViewWithFragmentComponentBuilder bind(ViewWithFragmentC.Builder builder);
  }

  @Component(
      modules = {
          ApplicationContextModule.class,
          DatabaseModule.class,
          DisasterWorker_HiltModule.class,
          ExpiryCheckWorker_HiltModule.class,
          HiltWrapper_FragmentGetContextFix_FragmentGetContextFixModule.class,
          HiltWrapper_WorkerFactoryModule.class,
          NetworkModule.class,
          RepositoryModule.class,
          ShardingWorker_HiltModule.class,
          SyncModule.class,
          SyncWorker_HiltModule.class,
          ActivityRetainedCBuilderModule.class,
          ServiceCBuilderModule.class,
          WorkerModule.class
      }
  )
  @Singleton
  public abstract static class SingletonC implements TillzoPOSApp_GeneratedInjector,
      FragmentGetContextFix.FragmentGetContextFixEntryPoint,
      HiltWrapper_ActivityRetainedComponentManager_ActivityRetainedComponentBuilderEntryPoint,
      ServiceComponentManager.ServiceComponentBuilderEntryPoint,
      SingletonComponent,
      GeneratedComponent {
  }

  @Subcomponent
  @ServiceScoped
  public abstract static class ServiceC implements ServiceComponent,
      GeneratedComponent {
    @Subcomponent.Builder
    abstract interface Builder extends ServiceComponentBuilder {
    }
  }

  @Subcomponent(
      modules = {
          BillingViewModel_HiltModules.KeyModule.class,
          CasioViewModel_HiltModules.KeyModule.class,
          CategoryManagementViewModel_HiltModules.KeyModule.class,
          CheckoutViewModel_HiltModules.KeyModule.class,
          CreateGrnViewModel_HiltModules.KeyModule.class,
          CreatePurchaseOrderViewModel_HiltModules.KeyModule.class,
          CrmViewModel_HiltModules.KeyModule.class,
          ExpenseViewModel_HiltModules.KeyModule.class,
          ForceUpdateViewModel_HiltModules.KeyModule.class,
          GrnDetailViewModel_HiltModules.KeyModule.class,
          GrnListViewModel_HiltModules.KeyModule.class,
          HiltWrapper_ActivityRetainedComponentManager_LifecycleModule.class,
          HiltWrapper_SavedStateHandleModule.class,
          HistoryViewModel_HiltModules.KeyModule.class,
          HomeViewModel_HiltModules.KeyModule.class,
          InlineScannerViewModel_HiltModules.KeyModule.class,
          InventoryCrudViewModel_HiltModules.KeyModule.class,
          LoginViewModel_HiltModules.KeyModule.class,
          LowStockViewModel_HiltModules.KeyModule.class,
          OcrEntryViewModel_HiltModules.KeyModule.class,
          PINUnlockViewModel_HiltModules.KeyModule.class,
          PODetailViewModel_HiltModules.KeyModule.class,
          PermissionManagerViewModel_HiltModules.KeyModule.class,
          PosViewModel_HiltModules.KeyModule.class,
          PrinterSettingsViewModel_HiltModules.KeyModule.class,
          ProductUnitsViewModel_HiltModules.KeyModule.class,
          PurchaseOrderListViewModel_HiltModules.KeyModule.class,
          QrGeneratorViewModel_HiltModules.KeyModule.class,
          ReturnsViewModel_HiltModules.KeyModule.class,
          ScannerViewModel_HiltModules.KeyModule.class,
          SettingsViewModel_HiltModules.KeyModule.class,
          SheetPickerViewModel_HiltModules.KeyModule.class,
          SignInViewModel_HiltModules.KeyModule.class,
          StatementViewModel_HiltModules.KeyModule.class,
          StockAdjustmentViewModel_HiltModules.KeyModule.class,
          TillViewModel_HiltModules.KeyModule.class,
          ActivityCBuilderModule.class,
          ViewModelCBuilderModule.class,
          UserManagementViewModel_HiltModules.KeyModule.class,
          VendorManagementViewModel_HiltModules.KeyModule.class,
          WastageViewModel_HiltModules.KeyModule.class,
          ZReportViewModel_HiltModules.KeyModule.class
      }
  )
  @ActivityRetainedScoped
  public abstract static class ActivityRetainedC implements ActivityRetainedComponent,
      ActivityComponentManager.ActivityComponentBuilderEntryPoint,
      HiltWrapper_ActivityRetainedComponentManager_ActivityRetainedLifecycleEntryPoint,
      GeneratedComponent {
    @Subcomponent.Builder
    abstract interface Builder extends ActivityRetainedComponentBuilder {
    }
  }

  @Subcomponent(
      modules = {
          HiltWrapper_ActivityModule.class,
          HiltWrapper_DefaultViewModelFactories_ActivityModule.class,
          FragmentCBuilderModule.class,
          ViewCBuilderModule.class
      }
  )
  @ActivityScoped
  public abstract static class ActivityC implements MainActivity_GeneratedInjector,
      ActivityComponent,
      DefaultViewModelFactories.ActivityEntryPoint,
      HiltWrapper_HiltViewModelFactory_ActivityCreatorEntryPoint,
      FragmentComponentManager.FragmentComponentBuilderEntryPoint,
      ViewComponentManager.ViewComponentBuilderEntryPoint,
      GeneratedComponent {
    @Subcomponent.Builder
    abstract interface Builder extends ActivityComponentBuilder {
    }
  }

  @Subcomponent(
      modules = {
          BillingViewModel_HiltModules.BindsModule.class,
          CasioViewModel_HiltModules.BindsModule.class,
          CategoryManagementViewModel_HiltModules.BindsModule.class,
          CheckoutViewModel_HiltModules.BindsModule.class,
          CreateGrnViewModel_HiltModules.BindsModule.class,
          CreatePurchaseOrderViewModel_HiltModules.BindsModule.class,
          CrmViewModel_HiltModules.BindsModule.class,
          ExpenseViewModel_HiltModules.BindsModule.class,
          ForceUpdateViewModel_HiltModules.BindsModule.class,
          GrnDetailViewModel_HiltModules.BindsModule.class,
          GrnListViewModel_HiltModules.BindsModule.class,
          HiltWrapper_HiltViewModelFactory_ViewModelModule.class,
          HistoryViewModel_HiltModules.BindsModule.class,
          HomeViewModel_HiltModules.BindsModule.class,
          InlineScannerViewModel_HiltModules.BindsModule.class,
          InventoryCrudViewModel_HiltModules.BindsModule.class,
          LoginViewModel_HiltModules.BindsModule.class,
          LowStockViewModel_HiltModules.BindsModule.class,
          OcrEntryViewModel_HiltModules.BindsModule.class,
          PINUnlockViewModel_HiltModules.BindsModule.class,
          PODetailViewModel_HiltModules.BindsModule.class,
          PermissionManagerViewModel_HiltModules.BindsModule.class,
          PosViewModel_HiltModules.BindsModule.class,
          PrinterSettingsViewModel_HiltModules.BindsModule.class,
          ProductUnitsViewModel_HiltModules.BindsModule.class,
          PurchaseOrderListViewModel_HiltModules.BindsModule.class,
          QrGeneratorViewModel_HiltModules.BindsModule.class,
          ReturnsViewModel_HiltModules.BindsModule.class,
          ScannerViewModel_HiltModules.BindsModule.class,
          SettingsViewModel_HiltModules.BindsModule.class,
          SheetPickerViewModel_HiltModules.BindsModule.class,
          SignInViewModel_HiltModules.BindsModule.class,
          StatementViewModel_HiltModules.BindsModule.class,
          StockAdjustmentViewModel_HiltModules.BindsModule.class,
          TillViewModel_HiltModules.BindsModule.class,
          UserManagementViewModel_HiltModules.BindsModule.class,
          VendorManagementViewModel_HiltModules.BindsModule.class,
          WastageViewModel_HiltModules.BindsModule.class,
          ZReportViewModel_HiltModules.BindsModule.class
      }
  )
  @ViewModelScoped
  public abstract static class ViewModelC implements ViewModelComponent,
      HiltViewModelFactory.ViewModelFactoriesEntryPoint,
      GeneratedComponent {
    @Subcomponent.Builder
    abstract interface Builder extends ViewModelComponentBuilder {
    }
  }

  @Subcomponent
  @ViewScoped
  public abstract static class ViewC implements ViewComponent,
      GeneratedComponent {
    @Subcomponent.Builder
    abstract interface Builder extends ViewComponentBuilder {
    }
  }

  @Subcomponent(
      modules = ViewWithFragmentCBuilderModule.class
  )
  @FragmentScoped
  public abstract static class FragmentC implements FragmentComponent,
      DefaultViewModelFactories.FragmentEntryPoint,
      ViewComponentManager.ViewWithFragmentComponentBuilderEntryPoint,
      GeneratedComponent {
    @Subcomponent.Builder
    abstract interface Builder extends FragmentComponentBuilder {
    }
  }

  @Subcomponent
  @ViewScoped
  public abstract static class ViewWithFragmentC implements ViewWithFragmentComponent,
      GeneratedComponent {
    @Subcomponent.Builder
    abstract interface Builder extends ViewWithFragmentComponentBuilder {
    }
  }
}
